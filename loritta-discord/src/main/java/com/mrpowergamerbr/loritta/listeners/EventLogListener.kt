package com.mrpowergamerbr.loritta.listeners

import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.dao.StoredMessage
import com.mrpowergamerbr.loritta.tables.ServerConfigs
import com.mrpowergamerbr.loritta.tables.StoredMessages
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.debug.DebugLog
import com.mrpowergamerbr.loritta.utils.eventlog.EventLog
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.guild.GuildBanEvent
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent
import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.dao.servers.moduleconfigs.EventLogConfig
import net.perfectdreams.loritta.tables.servers.moduleconfigs.EventLogConfigs
import net.perfectdreams.loritta.utils.CachedUserInfo
import net.perfectdreams.loritta.utils.DateUtils
import net.perfectdreams.loritta.utils.ImageFormat
import net.perfectdreams.loritta.utils.extensions.getEffectiveAvatarUrl
import org.apache.commons.io.IOUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
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

		val bannedUsers = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).maximumSize(100)
				.build<String, Boolean>()
	}

	override fun onUserUpdateAvatar(event: UserUpdateAvatarEvent) {
		if (DebugLog.cancelAllEvents)
			return

		if (loritta.rateLimitChecker.checkIfRequestShouldBeIgnored())
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

				val oldAvatarUrl = event.oldAvatarUrl
						?.replace("gif", "png")
						?: event.user.defaultAvatarUrl

				val rawOldAvatar = LorittaUtils.downloadImage(oldAvatarUrl)
				val rawNewAvatar = LorittaUtils.downloadImage(event.user.getEffectiveAvatarUrl(ImageFormat.PNG))

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

						loritta.newSuspendedTransaction {
							(ServerConfigs innerJoin EventLogConfigs)
									.select {
										EventLogConfigs.enabled eq true and
												(EventLogConfigs.avatarChanges eq true) and
												(ServerConfigs.id inList guilds.map { it.idLong })
									}
									.toList()
						}.forEach {
							val guildId = it[ServerConfigs.id].value
							val eventLogChannelId = it[EventLogConfigs.eventLogChannelId]

							val locale = loritta.getLocaleById(it[ServerConfigs.localeId])

							val guild = guilds.first { it.idLong == guildId }

							val textChannel = guild.getTextChannelById(eventLogChannelId)

							if (textChannel != null && textChannel.canTalk()) {
								if (!guild.selfMember.hasPermission(textChannel, Permission.MESSAGE_EMBED_LINKS))
									return@forEach
								if (!guild.selfMember.hasPermission(textChannel, Permission.MESSAGE_ATTACH_FILES))
									return@forEach
								if (!guild.selfMember.hasPermission(textChannel, Permission.VIEW_CHANNEL))
									return@forEach
								if (!guild.selfMember.hasPermission(textChannel, Permission.MESSAGE_READ))
									return@forEach

								embed.setDescription("\uD83D\uDDBC ${locale["modules.eventLog.avatarChanged", event.user.asMention]}")
								embed.setFooter(locale["modules.eventLog.userID", event.user.id], null)

								val message = MessageBuilder().append(" ").setEmbed(embed.build())

								textChannel.sendMessage(message.build()).addFile(bais, "avatar.png").queue()
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

	// Mensagens
	override fun onGuildMessageDelete(event: GuildMessageDeleteEvent) {
		if (DebugLog.cancelAllEvents)
			return

		if (loritta.rateLimitChecker.checkIfRequestShouldBeIgnored())
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
			val locale = loritta.getLocaleById(serverConfig.localeId)
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(com.mrpowergamerbr.loritta.utils.loritta, ServerConfig::eventLogConfig) ?: return@launch

			if (eventLogConfig.enabled && eventLogConfig.messageDeleted) {
				val textChannel = event.guild.getTextChannelById(eventLogConfig.eventLogChannelId)
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
					return@launch

				if (textChannel != null && textChannel.canTalk()) {
					val storedMessage = loritta.newSuspendedTransaction {
						StoredMessage.findById(event.messageIdLong)
					}

					if (storedMessage != null) {
						val user = lorittaShards.retrieveUserInfoById(storedMessage.authorId) ?: return@launch

						val webhook = EventLog.getOrCreateEventLogWebhook(event.guild, eventLogConfig) ?: return@launch

						val embed = WebhookEmbedBuilder()
						embed.setTimestamp(Instant.now())
						embed.setFooter(WebhookEmbed.EmbedFooter(locale["modules.eventLog.userID", user.id.toString()], null))
						embed.setColor(Color(221, 0, 0).rgb)

						embed.setAuthor(WebhookEmbed.EmbedAuthor(user.name + "#" + user.discriminator, null, user.effectiveAvatarUrl))

						var deletedMessage = "\uD83D\uDCDD ${locale.getList("modules.eventLog.messageDeleted", storedMessage.content, "<#${storedMessage.channelId}>").joinToString("\n")}"

						if (storedMessage.storedAttachments.isNotEmpty()) {
							deletedMessage += "\n${locale["modules.eventLog.messageDeletedUploads"]}\n" + storedMessage.storedAttachments.joinToString(separator = "\n")
						}

						embed.setDescription(deletedMessage)

						webhook.send(
								WebhookMessageBuilder()
										.setUsername(event.guild.selfMember.user.name)
										.setContent(" ")
										.setAvatarUrl(event.guild.selfMember.user.effectiveAvatarUrl)
										.addEmbeds(embed.build())
										.build()
						)

						loritta.newSuspendedTransaction {
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

		if (loritta.rateLimitChecker.checkIfRequestShouldBeIgnored())
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
			val locale = loritta.getLocaleById(serverConfig.localeId)
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(com.mrpowergamerbr.loritta.utils.loritta, ServerConfig::eventLogConfig) ?: return@launch

			if (eventLogConfig.enabled && eventLogConfig.messageDeleted) {
				val textChannel = event.guild.getTextChannelById(eventLogConfig.eventLogChannelId)
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
					return@launch

				if (textChannel != null && textChannel.canTalk()) {
					val storedMessages = loritta.newSuspendedTransaction {
						StoredMessage.find { StoredMessages.id inList event.messageIds.map { it.toLong() } }.toMutableList()
					}

					if (storedMessages.isNotEmpty()) {
						val retrievedUsers = mutableMapOf<Long, CachedUserInfo?>()

						val user = lorittaShards.retrieveUserInfoById(storedMessages.first().authorId)
								?: return@launch

						retrievedUsers[storedMessages.first().authorId] = user

						val embed = EmbedBuilder()
						embed.setTimestamp(Instant.now())
						embed.setColor(Color(221, 0, 0))
						embed.setAuthor(user.name + "#" + user.discriminator, null, user.effectiveAvatarUrl)

						val lines = mutableListOf<String>()

						for (message in storedMessages) {
							val messageSentByUser = retrievedUsers.getOrPut(message.authorId, { lorittaShards.retrieveUserInfoById(message.authorId) })

							val creationTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(message.createdAt), TimeZone.getTimeZone("GMT").toZoneId())

							val line = "[${creationTime.format(DateUtils.PRETTY_DATE_FORMAT)}] (${message.authorId}) ${messageSentByUser?.name}#${messageSentByUser?.discriminator}: ${message.content}"
							lines.add(line)
						}

						val targetStream = IOUtils.toInputStream(lines.joinToString("\n"), Charset.defaultCharset())

						val deletedMessage = "\uD83D\uDCDD ${locale["modules.eventLog.bulkDeleted"]}"

						embed.setDescription(deletedMessage)

						val channelName = event.guild.getTextChannelById(storedMessages.first().channelId)?.name ?: "unknown"

						textChannel.sendMessage(MessageBuilder().append(" ").setEmbed(embed.build()).build()).addFile(targetStream, "deleted-${event.guild.name}-$channelName-${DateUtils.PRETTY_FILE_SAFE_UNDERSCORE_DATE_FORMAT.format(Instant.now())}.log").queue()

						loritta.newSuspendedTransaction {
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

		if (loritta.rateLimitChecker.checkIfRequestShouldBeIgnored())
			return

		bannedUsers.put("${event.guild.id}#${event.user.id}", true)

		GlobalScope.launch(loritta.coroutineDispatcher) {
			// Fazer relay de bans
			if (event.guild.id == Constants.PORTUGUESE_SUPPORT_GUILD_ID) {
				val relayTo = lorittaShards.getGuildById(Constants.ENGLISH_SUPPORT_GUILD_ID)

				if (relayTo != null) {
					if (relayTo.retrieveBanList().await().firstOrNull { it.user == event.user } == null) {
						relayTo.ban(event.user, 7, "Banned on LorittaLand (Brazilian Server)").queue()
					}
				}
			}
			if (event.guild.id == Constants.ENGLISH_SUPPORT_GUILD_ID) {
				val relayTo = lorittaShards.getGuildById(Constants.PORTUGUESE_SUPPORT_GUILD_ID)

				if (relayTo != null) {
					if (relayTo.retrieveBanList().await().firstOrNull { it.user == event.user } == null) {
						relayTo.ban(event.user, 7, "Banido na LorittaLand (English Server)").queue()
					}
				}
			}

			val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(com.mrpowergamerbr.loritta.utils.loritta, ServerConfig::eventLogConfig) ?: return@launch

			if (eventLogConfig.enabled && eventLogConfig.memberBanned) {
				val textChannel = event.guild.getTextChannelById(eventLogConfig.eventLogChannelId) ?: return@launch
				val locale = loritta.getLocaleById(serverConfig.localeId)

				if (!textChannel.canTalk())
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
					return@launch

				val webhook = EventLog.getOrCreateEventLogWebhook(event.guild, eventLogConfig) ?: return@launch

				val embed = WebhookEmbedBuilder()
				embed.setTimestamp(Instant.now())
				embed.setColor(Color(35, 209, 96).rgb)

				val message = "\uD83D\uDEAB **${locale["modules.eventLog.banned", event.user.name]}**"

				embed.setAuthor(WebhookEmbed.EmbedAuthor("${event.user.name}#${event.user.discriminator}", null, event.user.effectiveAvatarUrl))
				embed.setDescription(message)
				embed.setFooter(WebhookEmbed.EmbedFooter(locale["modules.eventLog.userID", event.user.id], null))

				webhook.send(
						WebhookMessageBuilder()
								.setUsername(event.guild.selfMember.user.name)
								.setContent(" ")
								.setAvatarUrl(event.guild.selfMember.user.effectiveAvatarUrl)
								.addEmbeds(embed.build())
								.build()
				)
				return@launch
			}
		}
	}

	override fun onGuildUnban(event: GuildUnbanEvent) {
		if (DebugLog.cancelAllEvents)
			return

		if (loritta.rateLimitChecker.checkIfRequestShouldBeIgnored())
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

			val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(com.mrpowergamerbr.loritta.utils.loritta, ServerConfig::eventLogConfig) ?: return@launch

			if (eventLogConfig.enabled && eventLogConfig.memberUnbanned) {
				val textChannel = event.guild.getTextChannelById(eventLogConfig.eventLogChannelId) ?: return@launch
				val locale = loritta.getLocaleById(serverConfig.localeId)
				if (!textChannel.canTalk())
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
					return@launch

				val webhook = EventLog.getOrCreateEventLogWebhook(event.guild, eventLogConfig) ?: return@launch

				val embed = WebhookEmbedBuilder()
				embed.setTimestamp(Instant.now())
				embed.setColor(Color(35, 209, 96).rgb)

				val message = "\uD83E\uDD1D **${locale["modules.eventLog.unbanned", event.user.name]}**"

				embed.setAuthor(WebhookEmbed.EmbedAuthor("${event.user.name}#${event.user.discriminator}", null, event.user.effectiveAvatarUrl))
				embed.setDescription(message)
				embed.setFooter(WebhookEmbed.EmbedFooter(locale["modules.eventLog.userID", event.user.id], null))

				webhook.send(
						WebhookMessageBuilder()
								.setUsername(event.guild.selfMember.user.name)
								.setContent(" ")
								.setAvatarUrl(event.guild.selfMember.user.effectiveAvatarUrl)
								.addEmbeds(embed.build())
								.build()
				)
				return@launch
			}
		}
	}

	override fun onGuildMemberUpdateNickname(event: GuildMemberUpdateNicknameEvent) {
		if (DebugLog.cancelAllEvents)
			return

		if (loritta.rateLimitChecker.checkIfRequestShouldBeIgnored())
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(com.mrpowergamerbr.loritta.utils.loritta, ServerConfig::eventLogConfig) ?: return@launch

			if (eventLogConfig.enabled && eventLogConfig.nicknameChanges) {
				val locale = loritta.getLocaleById(serverConfig.localeId)
				val embed = WebhookEmbedBuilder()
				embed.setColor(Color(35, 209, 96).rgb)
				embed.setTimestamp(Instant.now())
				embed.setAuthor(WebhookEmbed.EmbedAuthor("${event.member.user.name}#${event.member.user.discriminator}", null, event.member.user.effectiveAvatarUrl))

				// ===[ NICKNAME ]===
				val textChannel = event.guild.getTextChannelById(eventLogConfig.eventLogChannelId) ?: return@launch
				if (!textChannel.canTalk())
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
					return@launch

				val webhook = EventLog.getOrCreateEventLogWebhook(event.guild, eventLogConfig) ?: return@launch

				val oldNickname = if (event.oldNickname == null) "\uD83E\uDD37 ${locale["modules.eventLog.noNickname"]}" else event.oldNickname
				val newNickname = if (event.newNickname == null) "\uD83E\uDD37 ${locale["modules.eventLog.noNickname"]}" else event.newNickname

				embed.setDescription("\uD83D\uDCDD ${locale.getList("modules.eventLog.nicknameChanged", oldNickname, newNickname).joinToString("\n")}")
				embed.setFooter(WebhookEmbed.EmbedFooter(locale["modules.eventLog.userID", event.member.user.id], null))

				webhook.send(
						WebhookMessageBuilder()
								.setUsername(event.guild.selfMember.user.name)
								.setContent(" ")
								.setAvatarUrl(event.guild.selfMember.user.effectiveAvatarUrl)
								.addEmbeds(embed.build())
								.build()
				)
				return@launch
			}
		}
	}
}
