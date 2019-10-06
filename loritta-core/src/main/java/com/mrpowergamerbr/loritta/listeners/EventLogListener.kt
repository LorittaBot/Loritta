package com.mrpowergamerbr.loritta.listeners

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.jsonObject
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.StoredMessage
import com.mrpowergamerbr.loritta.dao.UsernameChange
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.StoredMessages
import com.mrpowergamerbr.loritta.tables.UsernameChanges
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.debug.DebugLog
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.getTextChannelByNullableId
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.audit.ActionType
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.guild.GuildBanEvent
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent
import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent
import net.dv8tion.jda.api.events.user.update.UserUpdateDiscriminatorEvent
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.utils.DateUtils
import org.apache.commons.io.IOUtils
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

class EventLogListener(internal val loritta: Loritta) : ListenerAdapter() {
	companion object {
		private val logger = KotlinLogging.logger {}
		val downloadedAvatarJobs = ConcurrentHashMap<String, Job>()

		val handledUsernameChanges = Caffeine.newBuilder().expireAfterWrite(15, TimeUnit.SECONDS).maximumSize(100)
				.removalListener { k1: Long?, v1: UserMetaHolder?, removalCause ->
					// Removal listeners processam removals, expired, etc.
					// Então não precisamos usar o sendUsernameChange em outros lugares :3
					if (k1 != null && v1 != null) {
						GlobalScope.launch(loritta.coroutineDispatcher) {
							val user = lorittaShards.getUserById(k1) ?: return@launch
							sendUsernameChange(user, v1)
						}
					}
				}
				.build<Long, UserMetaHolder>().asMap()

		val cachedBeforeSending = Caffeine.newBuilder().expireAfterWrite(3, TimeUnit.SECONDS).maximumSize(100)
				.removalListener { k1: Long?, v1: UserMetaHolder?, removalCause ->
					// Para evitar que fique spammando o master cluster de requests, nós iremos manter os requests por 3 segundos antes de mandar
					if (v1 != null && k1 != null) {
						val shard = loritta.config.clusters.first { it.id == 1L }
						val id = k1
						val meta = v1

						logger.info { "Sending username/discriminator change for $id with data ${meta.oldName}#${meta.oldDiscriminator} to the master server..." }
						GlobalScope.launch(loritta.coroutineDispatcher) {
							HttpRequest.get("https://${shard.getUrl()}/api/v1/loritta/user/$id/username-change")
									.userAgent(loritta.lorittaCluster.getUserAgent())
									.header("Authorization", loritta.lorittaInternalApiKey.name)
									.connectTimeout(loritta.config.loritta.clusterConnectionTimeout)
									.readTimeout(loritta.config.loritta.clusterReadTimeout)
									.send(
											gson.toJson(
													jsonObject(
															"name" to meta.oldName,
															"discriminator" to meta.oldDiscriminator
													)
											)
									)
									.ok()
						}
					}
				}
				.build<Long, UserMetaHolder>().asMap()

		fun sendUsernameChange(user: User, usernameChange: UserMetaHolder) {
			val oldName = usernameChange.oldName ?: user.name
			val oldDiscriminator = usernameChange.oldDiscriminator ?: user.discriminator
			val newName = user.name
			val newDiscriminator = user.discriminator
			val embed = EmbedBuilder()
			embed.setTimestamp(Instant.now())
			embed.setAuthor("$newName#$newDiscriminator", null, user.effectiveAvatarUrl)
			embed.setColor(Constants.DISCORD_BLURPLE)

			val changedAt = System.currentTimeMillis()

			if (loritta.isMaster) { // Apenas a Lori mestre guarda as alterações necessárias :3
				transaction(Databases.loritta) {
					UsernameChange.new {
						userId = user.idLong
						username = newName
						discriminator = newDiscriminator
						writeValues[UsernameChanges.changedAt as Column<Any?>] = changedAt
					}
				}
			}

			val guilds = lorittaShards.getMutualGuilds(user)

			loritta.serversColl.find(
					Filters.and(
							Filters.eq("eventLogConfig.usernameChanges", true),
							Filters.eq("eventLogConfig.enabled", true),
							Filters.`in`("_id", guilds.map { it.id })
					)
			).iterator().use {
				while (it.hasNext()) {
					val config = it.next()
					val locale = loritta.getLegacyLocaleById(config.localeId)

					val guild = guilds.first { it.id == config.guildId }

					val textChannel = guild.getTextChannelByNullableId(config.eventLogConfig.eventLogChannelId)

					if (textChannel != null && textChannel.canTalk()) {
						if (!guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
							continue
						if (!guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
							continue
						if (!guild.selfMember.hasPermission(Permission.MESSAGE_READ))
							continue

						embed.setDescription("\uD83D\uDCDD ${locale["EVENTLOG_NAME_CHANGED", user.asMention, "$oldName#$oldDiscriminator", "$newName#$newDiscriminator"]}")
						embed.setFooter(locale["EVENTLOG_USER_ID", user.id], null)

						textChannel.sendMessage(embed.build()).queue()
					}
				}
			}
		}
	}

	class UserMetaHolder(var oldName: String?, var oldDiscriminator: String?)

	override fun onUserUpdateAvatar(event: UserUpdateAvatarEvent) {
		if (DebugLog.cancelAllEvents)
			return

		// Primeiro iremos baixar o avatar em uma task
		// Para não precisar baixar (número de shards) vezes (na pior das hipóteses), vamos criar uma task separada que irá baixar apenas uma vez
		// A task, ao finalizar, irá propagar para o resto dos servidores
		if (downloadedAvatarJobs[event.entity.id] != null) // Se já temos uma task ativa, vamos ignorar!
			return

		downloadedAvatarJobs[event.entity.id] = GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				logger.info("Baixando avatar de ${event.entity.id} para enviar no event log...")

				val embed = EmbedBuilder()
				embed.setTimestamp(Instant.now())
				embed.setAuthor("${event.user.name}#${event.user.discriminator}", null, event.user.effectiveAvatarUrl)
				embed.setColor(Constants.DISCORD_BLURPLE)
				embed.setImage("attachment://avatar.png")

				val rawOldAvatar = LorittaUtils.downloadImage(if (event.oldAvatarUrl == null) event.user.defaultAvatarUrl else event.oldAvatarUrl!!.replace("jpg", "png"))
				val rawNewAvatar = LorittaUtils.downloadImage(event.user.effectiveAvatarUrl.replace("jpg", "png"))

				if (rawOldAvatar == null || rawNewAvatar == null) { // As vezes o avatar pode ser null
					downloadedAvatarJobs.remove(event.entity.id)
					return@launch
				}

				val oldAvatar = rawOldAvatar.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH)
				val newAvatar = rawNewAvatar.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH)

				val base = BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB_PRE)
				val graphics = base.graphics
				graphics.drawImage(oldAvatar, 0, 0, null)
				graphics.drawImage(newAvatar, 128, 0, null)

				ByteArrayOutputStream().use { baos ->
					ImageIO.write(base, "png", baos)

					ByteArrayInputStream(baos.toByteArray()).use { bais ->
						// E agora nós iremos anunciar a troca para todos os servidores
						val guilds = event.jda.guilds.filter { it.isMember(event.user) }

						loritta.serversColl.find(
								Filters.and(
										Filters.eq("eventLogConfig.avatarChanges", true),
										Filters.eq("eventLogConfig.enabled", true),
										Filters.`in`("_id", guilds.map { it.id })
								)

						).iterator().use {
							while (it.hasNext()) {
								val config = it.next()
								val locale = loritta.getLegacyLocaleById(config.localeId)

								val guild = guilds.first { it.id == config.guildId }

								val textChannel = guild.getTextChannelByNullableId(config.eventLogConfig.eventLogChannelId)

								if (textChannel != null && textChannel.canTalk()) {
									if (!guild.selfMember.hasPermission(textChannel, Permission.MESSAGE_EMBED_LINKS))
										continue
									if (!guild.selfMember.hasPermission(textChannel, Permission.MESSAGE_ATTACH_FILES))
										continue
									if (!guild.selfMember.hasPermission(textChannel, Permission.VIEW_CHANNEL))
										continue
									if (!guild.selfMember.hasPermission(textChannel, Permission.MESSAGE_READ))
										continue

									embed.setDescription("\uD83D\uDDBC ${locale.get("EVENTLOG_AVATAR_CHANGED", event.user.asMention)}")
									embed.setFooter(locale["EVENTLOG_USER_ID", event.user.id], null)

									val message = MessageBuilder().append(" ").setEmbed(embed.build())

									textChannel.sendMessage(message.build()).addFile(bais, "avatar.png").queue()
								}
							}
						}
					}
				}
				downloadedAvatarJobs.remove(event.entity.id)
			} catch (e: Exception) {
				logger.error(e) { "Erro ao fazer download do avatar de ${event.entity.id} (Antigo: ${event.oldAvatarId} / Novo: ${event.newAvatarId})" }
				downloadedAvatarJobs.remove(event.entity.id)
			}
		}
	}

	override fun onUserUpdateName(event: UserUpdateNameEvent) {
		if (DebugLog.cancelAllEvents)
			return

		if (!handledUsernameChanges.containsKey(event.user.idLong)) {
			handledUsernameChanges[event.user.idLong] = UserMetaHolder(event.oldName, null)
		} else {
			val usernameChange = handledUsernameChanges[event.user.idLong]!!
			usernameChange.oldName = event.oldName
		}

		if (!loritta.isMaster) {
			if (!cachedBeforeSending.containsKey(event.user.idLong)) {
				cachedBeforeSending[event.user.idLong] = UserMetaHolder(event.oldName, null)
			} else {
				val usernameChange = cachedBeforeSending[event.user.idLong]!!
				usernameChange.oldName = event.oldName
			}
		}
	}

	override fun onUserUpdateDiscriminator(event: UserUpdateDiscriminatorEvent) {
		if (DebugLog.cancelAllEvents)
			return

		if (!handledUsernameChanges.containsKey(event.user.idLong)) {
			handledUsernameChanges[event.user.idLong] = UserMetaHolder(null, event.oldDiscriminator)
		} else {
			val usernameChange = handledUsernameChanges[event.user.idLong]!!
			usernameChange.oldDiscriminator = event.oldDiscriminator
		}

		if (!loritta.isMaster) {
			if (!cachedBeforeSending.containsKey(event.user.idLong)) {
				cachedBeforeSending[event.user.idLong] = UserMetaHolder(null, event.oldDiscriminator)
			} else {
				val usernameChange = cachedBeforeSending[event.user.idLong]!!
				usernameChange.oldDiscriminator = event.oldDiscriminator
			}
		}
	}

	// Mensagens
	override fun onGuildMessageDelete(event: GuildMessageDeleteEvent) {
		if (DebugLog.cancelAllEvents)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			val config = loritta.getServerConfigForGuild(event.guild.id)
			val locale = loritta.getLegacyLocaleById(config.localeId)
			val eventLogConfig = config.eventLogConfig

			if (eventLogConfig.isEnabled && eventLogConfig.messageDeleted) {
				val textChannel = event.guild.getTextChannelByNullableId(eventLogConfig.eventLogChannelId)
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
					return@launch

				if (textChannel != null && textChannel.canTalk()) {
					val storedMessage = transaction(Databases.loritta) {
						StoredMessage.findById(event.messageIdLong)
					}

					if (storedMessage != null) {
						val user = lorittaShards.retrieveUserById(storedMessage.authorId.toString()) ?: return@launch

						val embed = EmbedBuilder()
						embed.setTimestamp(Instant.now())

						embed.setColor(Color(221, 0, 0))

						embed.setAuthor(user.name + "#" + user.discriminator, null, user.effectiveAvatarUrl)

						var deletedMessage = "\uD83D\uDCDD ${locale["EVENTLOG_MESSAGE_DELETED", storedMessage.content, "<#${storedMessage.channelId}>"]}"

						if (event.guild.selfMember.hasPermission(Permission.VIEW_AUDIT_LOGS)) {
							val auditEntry = event.guild.retrieveAuditLogs().await().firstOrNull()

							if (auditEntry != null && auditEntry.type == ActionType.MESSAGE_DELETE) {
								if (auditEntry.targetIdLong == storedMessage.authorId) {
									deletedMessage += "\n" + locale["EVENTLOG_MESSAGE_DeletedBy", auditEntry.user?.asMention ?: "???"] + "\n"
								}
							}
						}

						if (storedMessage.storedAttachments.isNotEmpty()) {
							deletedMessage += "\n${locale.get("EVENTLOG_MESSAGE_DELETED_UPLOADS")}\n" + storedMessage.storedAttachments.joinToString(separator = "\n")
						}

						embed.setDescription(deletedMessage)

						textChannel.sendMessage(embed.build()).queue()

						transaction(Databases.loritta) {
							StoredMessages.deleteWhere { StoredMessages.id eq event.messageIdLong }
						}
						return@launch
					}
				}
			}
		}
	}

	override fun onMessageBulkDelete(event: MessageBulkDeleteEvent) {
		if (DebugLog.cancelAllEvents)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			val config = loritta.getServerConfigForGuild(event.guild.id)
			val locale = loritta.getLegacyLocaleById(config.localeId)
			val eventLogConfig = config.eventLogConfig

			if (eventLogConfig.isEnabled && eventLogConfig.messageDeleted) {
				val textChannel = event.guild.getTextChannelByNullableId(eventLogConfig.eventLogChannelId)
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
					return@launch

				if (textChannel != null && textChannel.canTalk()) {
					val storedMessages = transaction(Databases.loritta) {
						StoredMessage.find { StoredMessages.id inList event.messageIds.map { it.toLong() } }.toMutableList()
					}
					if (storedMessages.isNotEmpty()) {
						val retrievedUsers = mutableMapOf<Long, User?>()

						val user = lorittaShards.retrieveUserById(storedMessages.first().authorId)
								?: return@launch

						retrievedUsers[storedMessages.first().authorId] = user

						val embed = EmbedBuilder()
						embed.setTimestamp(Instant.now())
						embed.setColor(Color(221, 0, 0))
						embed.setAuthor(user.name + "#" + user.discriminator, null, user.effectiveAvatarUrl)

						val lines = mutableListOf<String>()

						for (message in storedMessages) {
							val messageSentByUser = retrievedUsers.getOrPut(message.authorId, { lorittaShards.retrieveUserById(storedMessages.first().authorId) })

							val creationTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(message.createdAt), TimeZone.getTimeZone("GMT").toZoneId())

							val line = "[${creationTime.format(DateUtils.PRETTY_DATE_FORMAT)}] (${message.authorId}) ${messageSentByUser?.name}#${messageSentByUser?.discriminator}: ${message.content}"
							lines.add(line)
						}

						val targetStream = IOUtils.toInputStream(lines.joinToString("\n"), Charset.defaultCharset())

						val deletedMessage = "\uD83D\uDCDD ${locale["EVENTLOG_BulkDeleted"]}"

						embed.setDescription(deletedMessage)

						val channelName = event.guild.getTextChannelById(storedMessages.first().channelId)?.name ?: "unknown"

						textChannel.sendMessage(MessageBuilder().append(" ").setEmbed(embed.build()).build()).addFile(targetStream, "deleted-${event.guild.name}-$channelName-${DateUtils.PRETTY_FILE_SAFE_UNDERSCORE_DATE_FORMAT.format(Instant.now())}.log").queue()

						transaction(Databases.loritta) {
							StoredMessages.deleteWhere { StoredMessages.id inList event.messageIds.map { it.toLong() } }
						}
						return@launch
					}
				}
			}
		}
	}

	override fun onGuildBan(event: GuildBanEvent) {
		if (DebugLog.cancelAllEvents)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			// Fazer relay de bans
			if (event.guild.id == Constants.PORTUGUESE_SUPPORT_GUILD_ID) {
				val relayTo = lorittaShards.getGuildById(Constants.ENGLISH_SUPPORT_GUILD_ID)

				if (relayTo != null) {
					if (relayTo.retrieveBanList().await().firstOrNull { it.user == event.user } == null) {
						relayTo.ban(event.user, 7, "Banned on LorittaLand (Brazilian Server)")?.queue()
					}
				}
			}
			if (event.guild.id == Constants.ENGLISH_SUPPORT_GUILD_ID) {
				val relayTo = lorittaShards.getGuildById(Constants.PORTUGUESE_SUPPORT_GUILD_ID)

				if (relayTo != null) {
					if (relayTo.retrieveBanList().await().firstOrNull { it.user == event.user } == null) {
						relayTo.ban(event.user, 7, "Banido na LorittaLand (English Server)")?.queue()
					}
				}
			}

			val serverConfig = loritta.getServerConfigForGuild(event.guild.id)
			val eventLogConfig = serverConfig.eventLogConfig

			if (eventLogConfig.isEnabled && eventLogConfig.memberBanned) {
				val textChannel = event.guild.getTextChannelByNullableId(eventLogConfig.eventLogChannelId) ?: return@launch
				val locale = loritta.getLegacyLocaleById(serverConfig.localeId)

				if (!textChannel.canTalk())
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
					return@launch

				val embed = EmbedBuilder()
				embed.setTimestamp(Instant.now())
				embed.setColor(Color(35, 209, 96))

				var message = "\uD83D\uDEAB **${locale["EVENTLOG_Banned", event.user.name]}**"

				if (event.guild.selfMember.hasPermission(Permission.VIEW_AUDIT_LOGS)) {
					// Caso a Loritta consiga ver o audit log, vamos pegar quem baniu e o motivo do ban!
					val auditLog = event.guild.retrieveAuditLogs().await().first()

					if (auditLog.type == ActionType.BAN) {
						message += "\n**${locale["BAN_PunishedBy"]}:** ${auditLog.user?.asMention ?: "???"}"
						message += "\n**${locale["BAN_PunishmentReason"]}:** `${if (auditLog.reason == null) "\uD83E\uDD37 Nenhum motivo" else auditLog.reason}`"
					}
				}
				embed.setAuthor("${event.user.name}#${event.user.discriminator}", null, event.user.effectiveAvatarUrl)
				embed.setDescription(message)
				embed.setFooter(locale["EVENTLOG_USER_ID", event.user.id], null)

				textChannel.sendMessage(embed.build()).queue()
				return@launch
			}
		}
	}

	override fun onGuildUnban(event: GuildUnbanEvent) {
		if (DebugLog.cancelAllEvents)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			// Fazer relay de unbans
			if (event.guild.id == Constants.PORTUGUESE_SUPPORT_GUILD_ID) {
				val relayTo = lorittaShards.getGuildById(Constants.ENGLISH_SUPPORT_GUILD_ID)

				relayTo?.unban(event.user)?.queue()
			}
			if (event.guild.id == Constants.ENGLISH_SUPPORT_GUILD_ID) {
				val relayTo = lorittaShards.getGuildById(Constants.PORTUGUESE_SUPPORT_GUILD_ID)

				relayTo?.unban(event.user)?.queue()
			}

			val serverConfig = loritta.getServerConfigForGuild(event.guild.id)
			val eventLogConfig = serverConfig.eventLogConfig

			if (eventLogConfig.isEnabled && eventLogConfig.memberUnbanned) {
				val textChannel = event.guild.getTextChannelByNullableId(eventLogConfig.eventLogChannelId) ?: return@launch
				val locale = loritta.getLegacyLocaleById(serverConfig.localeId)
				if (!textChannel.canTalk())
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
					return@launch

				val embed = EmbedBuilder()
				embed.setTimestamp(Instant.now())
				embed.setColor(Color(35, 209, 96))

				var message = "\uD83E\uDD1D **${locale["EVENTLOG_Unbanned", event.user.name]}**"

				if (event.guild.selfMember.hasPermission(Permission.VIEW_AUDIT_LOGS)) {
					// Caso a Loritta consiga ver o audit log, vamos pegar quem baniu e o motivo do ban!
					val auditLog = event.guild.retrieveAuditLogs().await().first()

					if (auditLog.type == ActionType.UNBAN) {
						message += "\n${locale["EVENTLOG_UnbannedBy", auditLog.user?.asMention ?: "???"]}"
					}
				}

				embed.setAuthor("${event.user.name}#${event.user.discriminator}", null, event.user.effectiveAvatarUrl)
				embed.setDescription(message)
				embed.setFooter(locale["EVENTLOG_USER_ID", event.user.id], null)

				textChannel.sendMessage(embed.build()).queue()
				return@launch
			}
		}
	}

	override fun onGuildMemberUpdateNickname(event: GuildMemberUpdateNicknameEvent) {
		if (DebugLog.cancelAllEvents)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			val serverConfig = loritta.getServerConfigForGuild(event.guild.id)
			val eventLogConfig = serverConfig.eventLogConfig

			if (eventLogConfig.isEnabled && eventLogConfig.nicknameChanges) {
				val locale = loritta.getLegacyLocaleById(serverConfig.localeId)
				val embed = EmbedBuilder()
				embed.setColor(Color(35, 209, 96))
				embed.setTimestamp(Instant.now())
				embed.setAuthor("${event.member.user.name}#${event.member.user.discriminator}", null, event.member.user.effectiveAvatarUrl)

				// ===[ NICKNAME ]===
				val textChannel = event.guild.getTextChannelByNullableId(eventLogConfig.eventLogChannelId) ?: return@launch
				if (!textChannel.canTalk())
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
					return@launch

				val oldNickname = if (event.oldNickname == null) "\uD83E\uDD37 ${locale["EVENTLOG_NoNickname"]}" else event.oldNickname
				val newNickname = if (event.newNickname == null) "\uD83E\uDD37 ${locale["EVENTLOG_NoNickname"]}" else event.newNickname

				embed.setDescription("\uD83D\uDCDD ${locale["EVENTLOG_NicknameChanged", oldNickname, newNickname]}")
				embed.setFooter(locale["EVENTLOG_USER_ID", event.member.user.id], null)

				textChannel.sendMessage(embed.build()).queue()
				return@launch
			}
		}
	}
}