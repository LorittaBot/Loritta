package net.perfectdreams.loritta.morenitta.listeners

import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.guild.GuildBanEvent
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.perfectdreams.loritta.cinnamon.pudding.tables.StoredMessages
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.EventLogConfigs
import net.perfectdreams.loritta.common.utils.DateUtils
import net.perfectdreams.loritta.discordchatmessagerenderer.savedmessage.SavedMessage
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.dao.StoredMessage
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.EventLogConfig
import net.perfectdreams.loritta.morenitta.messageverify.LoriMessageDataUtils
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.ImageFormat
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils
import net.perfectdreams.loritta.morenitta.utils.debug.DebugLog
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getEffectiveAvatarUrl
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import org.apache.commons.io.IOUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

class EventLogListener(internal val loritta: LorittaBot) : ListenerAdapter() {
	companion object {
		private val logger by HarmonyLoggerFactory.logger {}
		val downloadedAvatarJobs = ConcurrentHashMap<String, Job>()

		val bannedUsers = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).maximumSize(100)
			.build<String, Boolean>()
		private val prettyPrintJson = Json {
			prettyPrint = true
		}
	}

	override fun onUserUpdateAvatar(event: UserUpdateAvatarEvent) {
		if (DebugLog.cancelAllEvents)
			return

		// Ignoring bot avatar updates because *every time* Loritta (or any other big bot that shares a lot of servers with her) changes their avatar,
		// it would SPAM event log messages in a lot of servers, causing the global rate limit to be triggered. :(
		if (event.user.isBot)
			return

		// Primeiro iremos baixar o avatar em uma task
		// Para não precisar baixar (número de shards) vezes (na pior das hipóteses), vamos criar uma task separada que irá baixar apenas uma vez
		// A task, ao finalizar, irá propagar para o resto dos servidores
		if (downloadedAvatarJobs[event.entity.id] != null) // Se já temos uma task ativa, vamos ignorar!
			return

		downloadedAvatarJobs[event.entity.id] = GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				logger.info { "Baixando avatar de ${event.entity.id} para enviar no event log..." }

				val oldAvatarUrl = event.oldAvatarUrl
					?.replace("gif", "png")
					?: event.user.defaultAvatarUrl

				val rawOldAvatar = LorittaUtils.downloadImage(loritta, oldAvatarUrl)
				val rawNewAvatar = LorittaUtils.downloadImage(loritta, event.user.getEffectiveAvatarUrl(ImageFormat.PNG))

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
								.selectAll()
								.where {
									EventLogConfigs.enabled eq true and
											(EventLogConfigs.avatarChanges eq true) and
											(ServerConfigs.id inList guilds.map { it.idLong })
								}
								.toList()
						}.forEach {
							val guildId = it[ServerConfigs.id].value
							val eventLogChannelId = it[EventLogConfigs.avatarChangesLogChannelId] ?: it[EventLogConfigs.eventLogChannelId]
							val eventLogConfig = loritta.newSuspendedTransaction { EventLogConfig.wrapRow(it) }

							val i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(it[ServerConfigs.localeId])

							val guild = guilds.first { it.idLong == guildId }

							val textChannel = guild.getGuildMessageChannelById(eventLogChannelId)

							if (textChannel != null && textChannel.canTalk()) {
								if (!guild.selfMember.hasPermission(textChannel, Permission.MESSAGE_EMBED_LINKS))
									return@forEach
								if (!guild.selfMember.hasPermission(textChannel, Permission.MESSAGE_ATTACH_FILES))
									return@forEach
								if (!guild.selfMember.hasPermission(textChannel, Permission.VIEW_CHANNEL))
									return@forEach

								val embed = EmbedBuilder()
								embed.setTimestamp(Instant.now())
								embed.setAuthor("${event.user.name}#${event.user.discriminator}", null, event.user.effectiveAvatarUrl)
								embed.setColor(Constants.DISCORD_BLURPLE.rgb)
								embed.setImage("attachment://avatar.png")

								embed.setDescription("\uD83D\uDDBC ${i18nContext.get(I18nKeysData.Modules.EventLog.AvatarChanged(userMention = event.user.asMention))}")
								embed.setFooter(i18nContext.get(I18nKeysData.Modules.EventLog.UserId(userId = event.user.id)), null)

								val message = MessageCreateBuilder()
									.setContent(" ")
									.addEmbeds(embed.build())
									.addFiles(FileUpload.fromData(bais, "avatar.png"))


								textChannel.sendMessage(message.build()).await()
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
	override fun onMessageDelete(event: MessageDeleteEvent) {
		if (DebugLog.cancelAllEvents)
			return

		if (!event.isFromGuild)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
			val i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return@launch

			if (eventLogConfig.enabled && eventLogConfig.messageDeleted) {
				val textChannel = event.guild.getGuildMessageChannelById(eventLogConfig.messageDeletedLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return@launch

				val storedMessage = loritta.newSuspendedTransaction {
					val storedMessage = StoredMessage.findById(event.messageIdLong)
					// Always delete the stored message no matter what
					StoredMessages.deleteWhere { StoredMessages.id eq event.messageIdLong }
					storedMessage
				}

				if (storedMessage != null && textChannel.canTalk() && event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) && event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL) && event.guild.selfMember.hasPermission(Permission.MESSAGE_ATTACH_FILES)) {
					HarmonyLoggerFactory.logger {}.value.info { "EventLogListener#retrieveUserInfoById (delete) - UserId: ${storedMessage.authorId}" }
					val user = loritta.lorittaShards.retrieveUserInfoById(storedMessage.authorId) ?: return@launch

					val embed = EmbedBuilder()
					embed.setTimestamp(Instant.now())
					embed.setFooter(i18nContext.get(I18nKeysData.Modules.EventLog.UserId(userId = user.id.toString())), null)
					embed.setColor(Color(221, 0, 0).rgb)

					embed.setAuthor(user.name + "#" + user.discriminator, null, user.effectiveAvatarUrl)

					val savedMessage = storedMessage.decryptContent(loritta)
					var deletedMessage = "\uD83D\uDCDD ${i18nContext.get(I18nKeysData.Modules.EventLog.MessageDeleted(messageContent = savedMessage.content, channelMention = "<#${storedMessage.channelId}>")).joinToString("\n")}"

					if (savedMessage.attachments.isNotEmpty()) {
						// We use proxy URL due to this: https://i.imgur.com/VyVlzVe.png
						val storedAttachments = savedMessage.attachments.map {
							it.proxyUrl
						}
						deletedMessage += "\n${i18nContext.get(I18nKeysData.Modules.EventLog.MessageDeletedUploads)}\n" + storedAttachments.joinToString(separator = "\n")
					}

					val fileName = LoriMessageDataUtils.createFileNameForSavedMessageImage(savedMessage)
					embed.setImage("attachment://$fileName")
					embed.setDescription(deletedMessage)

					val finalImage = LoriMessageDataUtils.createSignedRenderedSavedMessage(loritta, savedMessage, true)

					textChannel.sendMessageEmbeds(embed.build())
						.addFiles(FileUpload.fromData(finalImage, fileName))
						.await()
					return@launch
				}
			}
		}
	}

	override fun onMessageBulkDelete(event: MessageBulkDeleteEvent) {
		if (DebugLog.cancelAllEvents)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
			val i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return@launch

			if (eventLogConfig.enabled && eventLogConfig.messageDeleted) {
				val textChannel = event.guild.getGuildMessageChannelById(eventLogConfig.messageDeletedLogChannelId ?: eventLogConfig.eventLogChannelId)
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return@launch

				if (textChannel != null && textChannel.canTalk()) {
					val storedMessages = loritta.newSuspendedTransaction {
						StoredMessage.find { StoredMessages.id inList event.messageIds.map { it.toLong() } }.toMutableList()
					}

					if (storedMessages.isNotEmpty()) {
						val retrievedUsers = mutableMapOf<Long, CachedUserInfo?>()

						HarmonyLoggerFactory.logger {}.value.info { "EventLogListener#retrieveUserInfoById (bulk delete) - UserId: ${storedMessages.first().authorId}" }
						val user = loritta.lorittaShards.retrieveUserInfoById(storedMessages.first().authorId)
							?: return@launch

						retrievedUsers[storedMessages.first().authorId] = user

						val embed = EmbedBuilder()
						embed.setTimestamp(Instant.now())
						embed.setColor(Color(221, 0, 0).rgb)
						embed.setAuthor(user.name + "#" + user.discriminator, null, user.effectiveAvatarUrl)

						val lines = mutableListOf<String>()

						val savedMessages = mutableListOf<SavedMessage>()

						for (message in storedMessages) {
							val messageSentByUser = retrievedUsers.getOrPut(message.authorId) {
								loritta.lorittaShards.retrieveUserInfoById(
									message.authorId
								)
							}
							val savedMessage = message.decryptContent(loritta)
							savedMessages.add(savedMessage)

							val creationTime = savedMessage.timeCreated.atZoneSameInstant(TimeZone.getTimeZone("GMT").toZoneId())

							val line = "[${creationTime.format(DateUtils.PRETTY_DATE_FORMAT)}] (${message.authorId}) ${messageSentByUser?.name}#${messageSentByUser?.discriminator}: ${savedMessage.content}"
							lines.add(line)
						}

						val targetStream = IOUtils.toInputStream(lines.joinToString("\n"), Charset.defaultCharset())

						val deletedMessage = "\uD83D\uDCDD ${i18nContext.get(I18nKeysData.Modules.EventLog.BulkDeleted)}"

						embed.setDescription(deletedMessage)

						val channelName = event.guild.getGuildMessageChannelById(storedMessages.first().channelId)?.name ?: "unknown"

						textChannel
							.sendMessage(
								MessageCreateBuilder()
									.setContent(" ")
									.addEmbeds(embed.build())
									.addFiles(FileUpload.fromData(targetStream, "deleted-${event.guild.name}-$channelName-${DateUtils.PRETTY_FILE_SAFE_UNDERSCORE_DATE_FORMAT.format(Instant.now())}.log"))
									.addFiles(FileUpload.fromData(prettyPrintJson.encodeToString(savedMessages).toByteArray(Charsets.UTF_8), "deleted-${event.guild.name}-$channelName-${DateUtils.PRETTY_FILE_SAFE_UNDERSCORE_DATE_FORMAT.format(Instant.now())}.json"))
									.build(),
							)
							.await()

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

		bannedUsers.put("${event.guild.id}#${event.user.id}", true)

		GlobalScope.launch(loritta.coroutineDispatcher) {
			val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return@launch

			if (eventLogConfig.enabled && eventLogConfig.memberBanned) {
				val textChannel = event.guild.getGuildMessageChannelById(eventLogConfig.memberBannedLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return@launch
				val i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

				if (!textChannel.canTalk())
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return@launch

				val embed = EmbedBuilder()
				embed.setTimestamp(Instant.now())
				embed.setColor(Color(35, 209, 96).rgb)

				val message = "\uD83D\uDEAB **${i18nContext.get(I18nKeysData.Modules.EventLog.Banned(username = event.user.name))}**"

				embed.setAuthor("${event.user.name}#${event.user.discriminator}", null, event.user.effectiveAvatarUrl)
				embed.setDescription(message)
				embed.setFooter(i18nContext.get(I18nKeysData.Modules.EventLog.UserId(userId = event.user.id)), null)

				textChannel.sendMessageEmbeds(embed.build()).await()
				return@launch
			}
		}
	}

	override fun onGuildUnban(event: GuildUnbanEvent) {
		if (DebugLog.cancelAllEvents)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			// Fazer relay de unbans
			if (event.guild.idLong == Constants.PORTUGUESE_SUPPORT_GUILD_ID) {
				val relayTo = loritta.lorittaShards.getGuildById(Constants.ENGLISH_SUPPORT_GUILD_ID)

				relayTo?.unban(event.user)?.queue()
			}
			if (event.guild.idLong == Constants.ENGLISH_SUPPORT_GUILD_ID) {
				val relayTo = loritta.lorittaShards.getGuildById(Constants.PORTUGUESE_SUPPORT_GUILD_ID)

				relayTo?.unban(event.user)?.queue()
			}

			val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return@launch

			if (eventLogConfig.enabled && eventLogConfig.memberUnbanned) {
				val textChannel = event.guild.getGuildMessageChannelById(eventLogConfig.memberUnbannedLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return@launch
				val i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)
				if (!textChannel.canTalk())
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return@launch

				val embed = EmbedBuilder()
				embed.setTimestamp(Instant.now())
				embed.setColor(Color(35, 209, 96).rgb)

				val message = "\uD83E\uDD1D **${i18nContext.get(I18nKeysData.Modules.EventLog.Unbanned(username = event.user.name))}**"

				embed.setAuthor("${event.user.name}#${event.user.discriminator}", null, event.user.effectiveAvatarUrl)
				embed.setDescription(message)
				embed.setFooter(i18nContext.get(I18nKeysData.Modules.EventLog.UserId(userId = event.user.id)), null)

				textChannel.sendMessageEmbeds(embed.build()).await()
				return@launch
			}
		}
	}

	override fun onGuildMemberUpdateNickname(event: GuildMemberUpdateNicknameEvent) {
		if (DebugLog.cancelAllEvents)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return@launch

			if (eventLogConfig.enabled && eventLogConfig.nicknameChanges) {
				val i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)
				val embed = EmbedBuilder()
				embed.setColor(Color(35, 209, 96).rgb)
				embed.setTimestamp(Instant.now())
				embed.setAuthor("${event.member.user.name}#${event.member.user.discriminator}", null, event.member.user.effectiveAvatarUrl)

				// ===[ NICKNAME ]===
				val textChannel = event.guild.getGuildMessageChannelById(eventLogConfig.nicknameChangesLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return@launch
				if (!textChannel.canTalk())
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return@launch
				if (!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return@launch

				val oldNickname = event.oldNickname ?: "\uD83E\uDD37 ${i18nContext.get(I18nKeysData.Modules.EventLog.NoNickname)}"
				val newNickname = event.newNickname ?: "\uD83E\uDD37 ${i18nContext.get(I18nKeysData.Modules.EventLog.NoNickname)}"

				embed.setDescription("\uD83D\uDCDD ${i18nContext.get(I18nKeysData.Modules.EventLog.NicknameChanged(oldNickname = oldNickname, newNickname = newNickname)).joinToString("\n")}")
				embed.setFooter(i18nContext.get(I18nKeysData.Modules.EventLog.UserId(userId = event.member.user.id)), null)

				textChannel.sendMessageEmbeds(embed.build()).await()
				return@launch
			}
		}
	}
}
