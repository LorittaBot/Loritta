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
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent
import net.dv8tion.jda.api.events.guild.GuildBanEvent
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.events.role.RoleCreateEvent
import net.dv8tion.jda.api.events.role.RoleDeleteEvent
import net.dv8tion.jda.api.events.role.update.*
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
		val bannedUsers = Caffeine.newBuilder()
			.expireAfterWrite(10, TimeUnit.SECONDS)
			.maximumSize(100)
			.build<String, Boolean>()
		private val prettyPrintJson = Json { prettyPrint = true }
	}

	override fun onMessageReceived(event: MessageReceivedEvent) {
		if (DebugLog.cancelAllEvents || !event.isFromGuild || event.author.isBot)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
				val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
					loritta, ServerConfig::eventLogConfig
				) ?: return@launch

				if (eventLogConfig.enabled && (eventLogConfig.messageDeleted || eventLogConfig.messageEdited)) {
					val savedMessage = LoriMessageDataUtils.convertMessageToSavedMessage(event.message)

					loritta.newSuspendedTransaction {
						StoredMessage.new(event.messageIdLong) {
							this.authorId = event.author.idLong
							this.channelId = event.channel.idLong
							this.createdAt = Instant.now()
							this.savedMessageDataVersion = 1
							this.encryptAndSetContent(loritta, savedMessage)
						}
					}
				}
			} catch (e: Exception) {
				logger.error(e) { "Erro ao salvar mensagem do event log" }
			}
		}
	}

	override fun onMessageUpdate(event: MessageUpdateEvent) {
		if (DebugLog.cancelAllEvents || !event.isFromGuild || event.author.isBot)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
				val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
				val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
					loritta, ServerConfig::eventLogConfig
				) ?: return@launch

				if (eventLogConfig.enabled && eventLogConfig.messageEdited) {
					val storedMessage = loritta.newSuspendedTransaction {
						StoredMessage.findById(event.messageIdLong)
					}

					if (storedMessage != null) {
						val savedMessage = storedMessage.decryptContent(loritta)

						loritta.newSuspendedTransaction {
							storedMessage.encryptAndSetContent(
								loritta,
								LoriMessageDataUtils.convertMessageToSavedMessage(event.message)
							)
						}

						val textChannel = event.guild.getGuildMessageChannelById(
							eventLogConfig.messageEditedLogChannelId ?: eventLogConfig.eventLogChannelId
						) ?: return@launch

						if (textChannel.canTalk() &&
							event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) &&
							event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL) &&
							event.guild.selfMember.hasPermission(Permission.MESSAGE_ATTACH_FILES)) {

							if (savedMessage.content != event.message.contentRaw) {
								val embed = EmbedBuilder()
									.setColor(Color(238, 241, 0).rgb)
									.setDescription(
										"\uD83D\uDCDD ${
											locale.getList(
												"modules.eventLog.messageEdited",
												event.member?.asMention,
												savedMessage.content,
												event.message.contentRaw,
												event.guildChannel.asMention
											).joinToString("\n")
										}"
									)
									.setAuthor(
										"${event.author.name}#${event.author.discriminator}",
										null,
										event.author.effectiveAvatarUrl
									)
									.setFooter(locale["modules.eventLog.userID", event.author.id], null)
									.setTimestamp(Instant.now())

								val fileName = LoriMessageDataUtils.createFileNameForSavedMessageImage(savedMessage)
								embed.setImage("attachment://$fileName")

								val finalImage = LoriMessageDataUtils.createSignedRenderedSavedMessage(
									loritta, savedMessage, true
								)

								textChannel.sendMessageEmbeds(embed.build())
									.addFiles(FileUpload.fromData(finalImage, fileName))
									.await()
							}
						}
					}
				}
			} catch (e: Exception) {
				logger.error(e) { "Erro ao atualizar mensagem do event log" }
			}
		}
	}

	override fun onMessageDelete(event: MessageDeleteEvent) {
		if (DebugLog.cancelAllEvents || !event.isFromGuild)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
				val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
				val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
					loritta, ServerConfig::eventLogConfig
				) ?: return@launch

				if (eventLogConfig.enabled && eventLogConfig.messageDeleted) {
					val textChannel = event.guild.getGuildMessageChannelById(
						eventLogConfig.messageDeletedLogChannelId ?: eventLogConfig.eventLogChannelId
					) ?: return@launch

					val storedMessage = loritta.newSuspendedTransaction {
						val stored = StoredMessage.findById(event.messageIdLong)
						StoredMessages.deleteWhere { StoredMessages.id eq event.messageIdLong }
						stored
					}

					if (storedMessage != null && textChannel.canTalk() &&
						event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) &&
						event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL) &&
						event.guild.selfMember.hasPermission(Permission.MESSAGE_ATTACH_FILES)) {

						val user = loritta.lorittaShards.retrieveUserInfoById(storedMessage.authorId)
							?: return@launch

						val embed = EmbedBuilder()
							.setTimestamp(Instant.now())
							.setFooter(locale["modules.eventLog.userID", user.id.toString()], null)
							.setColor(Color(221, 0, 0).rgb)
							.setAuthor(user.name + "#" + user.discriminator, null, user.effectiveAvatarUrl)

						val savedMessage = storedMessage.decryptContent(loritta)
						var deletedMessage = "\uD83D\uDCDD ${
							locale.getList(
								"modules.eventLog.messageDeleted",
								savedMessage.content,
								"<#${storedMessage.channelId}>"
							).joinToString("\n")
						}"

						if (savedMessage.attachments.isNotEmpty()) {
							val storedAttachments = savedMessage.attachments.map { it.proxyUrl }
							deletedMessage += "\n${locale["modules.eventLog.messageDeletedUploads"]}\n" +
									storedAttachments.joinToString(separator = "\n")
						}

						val fileName = LoriMessageDataUtils.createFileNameForSavedMessageImage(savedMessage)
						embed.setImage("attachment://$fileName")
						embed.setDescription(deletedMessage)

						val finalImage = LoriMessageDataUtils.createSignedRenderedSavedMessage(
							loritta, savedMessage, true
						)

						textChannel.sendMessageEmbeds(embed.build())
							.addFiles(FileUpload.fromData(finalImage, fileName))
							.await()
					}
				}
			} catch (e: Exception) {
				logger.error(e) { "Erro ao processar deleção de mensagem" }
			}
		}
	}

	override fun onMessageBulkDelete(event: MessageBulkDeleteEvent) {
		if (DebugLog.cancelAllEvents)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
				val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
				val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
					loritta, ServerConfig::eventLogConfig
				) ?: return@launch

				if (eventLogConfig.enabled && eventLogConfig.messageDeleted) {
					val textChannel = event.guild.getGuildMessageChannelById(
						eventLogConfig.messageDeletedLogChannelId ?: eventLogConfig.eventLogChannelId
					)

					if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) ||
						!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
						return@launch

					if (textChannel != null && textChannel.canTalk()) {
						val storedMessages = loritta.newSuspendedTransaction {
							StoredMessage.find {
								StoredMessages.id inList event.messageIds.map { it.toLong() }
							}.toMutableList()
						}

						if (storedMessages.isNotEmpty()) {
							val retrievedUsers = mutableMapOf<Long, CachedUserInfo?>()
							val user = loritta.lorittaShards.retrieveUserInfoById(storedMessages.first().authorId)
								?: return@launch

							retrievedUsers[storedMessages.first().authorId] = user

							val embed = EmbedBuilder()
								.setTimestamp(Instant.now())
								.setColor(Color(221, 0, 0).rgb)
								.setAuthor(user.name + "#" + user.discriminator, null, user.effectiveAvatarUrl)

							val lines = mutableListOf<String>()
							val savedMessages = mutableListOf<SavedMessage>()

							for (message in storedMessages) {
								val messageSentByUser = retrievedUsers.getOrPut(message.authorId) {
									loritta.lorittaShards.retrieveUserInfoById(message.authorId)
								}
								val savedMessage = message.decryptContent(loritta)
								savedMessages.add(savedMessage)

								val creationTime = savedMessage.timeCreated.atZoneSameInstant(
									TimeZone.getTimeZone("GMT").toZoneId()
								)

								val line = "[${creationTime.format(DateUtils.PRETTY_DATE_FORMAT)}] " +
										"(${message.authorId}) ${messageSentByUser?.name}#${messageSentByUser?.discriminator}: " +
										savedMessage.content
								lines.add(line)
							}

							val targetStream = IOUtils.toInputStream(
								lines.joinToString("\n"),
								Charset.defaultCharset()
							)

							embed.setDescription("\uD83D\uDCDD ${locale["modules.eventLog.bulkDeleted"]}")

							val channelName = event.guild.getGuildMessageChannelById(
								storedMessages.first().channelId
							)?.name ?: "unknown"

							textChannel.sendMessage(
								MessageCreateBuilder()
									.setContent(" ")
									.addEmbeds(embed.build())
									.addFiles(
										FileUpload.fromData(
											targetStream,
											"deleted-${event.guild.name}-$channelName-${
												DateUtils.PRETTY_FILE_SAFE_UNDERSCORE_DATE_FORMAT.format(Instant.now())
											}.log"
										)
									)
									.addFiles(
										FileUpload.fromData(
											prettyPrintJson.encodeToString(savedMessages).toByteArray(Charsets.UTF_8),
											"deleted-${event.guild.name}-$channelName-${
												DateUtils.PRETTY_FILE_SAFE_UNDERSCORE_DATE_FORMAT.format(Instant.now())
											}.json"
										)
									)
									.build()
							).await()

							loritta.newSuspendedTransaction {
								StoredMessages.deleteWhere {
									StoredMessages.id inList event.messageIds.map { it.toLong() }
								}
							}
						}
					}
				}
			} catch (e: Exception) {
				logger.error(e) { "Erro ao processar deleção em massa" }
			}
		}
	}

	override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
		if (DebugLog.cancelAllEvents)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
				val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
				val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
					loritta, ServerConfig::eventLogConfig
				) ?: return@launch

				if (eventLogConfig.enabled && eventLogConfig.memberJoin) {
					val textChannel = event.guild.getGuildMessageChannelById(
						eventLogConfig.memberJoinLogChannelId ?: eventLogConfig.eventLogChannelId
					) ?: return@launch

					if (textChannel.canTalk() &&
						event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) &&
						event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {

						val embed = EmbedBuilder()
							.setColor(Color(87, 242, 135).rgb)
							.setDescription("\uD83D\uDC4B ${locale["modules.eventLog.memberJoin", event.member.asMention]}")
							.setAuthor(
								"${event.user.name}#${event.user.discriminator}",
								null,
								event.user.effectiveAvatarUrl
							)
							.setFooter(locale["modules.eventLog.userID", event.user.id], null)
							.setTimestamp(Instant.now())

						textChannel.sendMessageEmbeds(embed.build()).await()
					}
				}
			} catch (e: Exception) {
				logger.error(e) { "Erro ao processar entrada de membro" }
			}
		}
	}

	override fun onGuildMemberRemove(event: GuildMemberRemoveEvent) {
		if (DebugLog.cancelAllEvents)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
				val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
				val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
					loritta, ServerConfig::eventLogConfig
				) ?: return@launch

				if (eventLogConfig.enabled && eventLogConfig.memberLeave) {
					val textChannel = event.guild.getGuildMessageChannelById(
						eventLogConfig.memberLeaveLogChannelId ?: eventLogConfig.eventLogChannelId
					) ?: return@launch

					if (textChannel.canTalk() &&
						event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) &&
						event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {

						val embed = EmbedBuilder()
							.setColor(Color(221, 0, 0).rgb)
							.setDescription("\uD83D\uDC4B ${locale["modules.eventLog.memberLeave", event.user.asMention]}")
							.setAuthor(
								"${event.user.name}#${event.user.discriminator}",
								null,
								event.user.effectiveAvatarUrl
							)
							.setFooter(locale["modules.eventLog.userID", event.user.id], null)
							.setTimestamp(Instant.now())

						textChannel.sendMessageEmbeds(embed.build()).await()
					}
				}
			} catch (e: Exception) {
				logger.error(e) { "Erro ao processar saída de membro" }
			}
		}
	}

	override fun onGuildMemberUpdateNickname(event: GuildMemberUpdateNicknameEvent) {
		if (DebugLog.cancelAllEvents)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
				val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
				val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
					loritta, ServerConfig::eventLogConfig
				) ?: return@launch

				if (eventLogConfig.enabled && eventLogConfig.nicknameChanges) {
					val textChannel = event.guild.getGuildMessageChannelById(
						eventLogConfig.nicknameChangesLogChannelId ?: eventLogConfig.eventLogChannelId
					) ?: return@launch

					if (!textChannel.canTalk() ||
						!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) ||
						!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
						return@launch

					val oldNickname = event.oldNickname ?: "\uD83E\uDD37 ${locale["modules.eventLog.noNickname"]}"
					val newNickname = event.newNickname ?: "\uD83E\uDD37 ${locale["modules.eventLog.noNickname"]}"

					val embed = EmbedBuilder()
						.setColor(Color(238, 241, 0).rgb)
						.setTimestamp(Instant.now())
						.setAuthor(
							"${event.member.user.name}#${event.member.user.discriminator}",
							null,
							event.member.user.effectiveAvatarUrl
						)
						.setDescription(
							"\uD83D\uDCDD ${
								locale.getList(
									"modules.eventLog.nicknameChanged",
									oldNickname,
									newNickname
								).joinToString("\n")
							}"
						)
						.setFooter(locale["modules.eventLog.userID", event.member.user.id], null)

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			} catch (e: Exception) {
				logger.error(e) { "Erro ao processar mudança de nickname" }
			}
		}
	}

	override fun onUserUpdateAvatar(event: UserUpdateAvatarEvent) {
		if (DebugLog.cancelAllEvents || event.user.isBot)
			return

		if (downloadedAvatarJobs[event.entity.id] != null)
			return

		downloadedAvatarJobs[event.entity.id] = GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				logger.info { "Baixando avatar de ${event.entity.id} para enviar no event log..." }

				val oldAvatarUrl = event.oldAvatarUrl?.replace("gif", "png") ?: event.user.defaultAvatarUrl
				val rawOldAvatar = LorittaUtils.downloadImage(loritta, oldAvatarUrl)
				val rawNewAvatar = LorittaUtils.downloadImage(loritta, event.user.getEffectiveAvatarUrl(ImageFormat.PNG))

				if (rawOldAvatar == null || rawNewAvatar == null) {
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
							val locale = loritta.localeManager.getLocaleById(it[ServerConfigs.localeId])
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
									.setTimestamp(Instant.now())
									.setAuthor("${event.user.name}#${event.user.discriminator}", null, event.user.effectiveAvatarUrl)
									.setColor(Constants.DISCORD_BLURPLE.rgb)
									.setImage("attachment://avatar.png")
									.setDescription("\uD83D\uDDBC ${locale["modules.eventLog.avatarChanged", event.user.asMention]}")
									.setFooter(locale["modules.eventLog.userID", event.user.id], null)

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
				logger.error(e) { "Erro ao fazer download do avatar de ${event.entity.id}" }
				downloadedAvatarJobs.remove(event.entity.id)
			}
		}
	}

	override fun onRoleCreate(event: RoleCreateEvent) {
		if (DebugLog.cancelAllEvents)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
				val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
				val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
					loritta, ServerConfig::eventLogConfig
				) ?: return@launch

				if (eventLogConfig.enabled && eventLogConfig.roleCreate) {
					val textChannel = event.guild.getGuildMessageChannelById(
						eventLogConfig.roleCreateLogChannelId ?: eventLogConfig.eventLogChannelId
					) ?: return@launch

					if (textChannel.canTalk() &&
						event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) &&
						event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {

						val embed = EmbedBuilder()
							.setColor(Color(87, 242, 135).rgb)
							.setDescription("\uD83C\uDFF7️ ${locale["modules.eventLog.roleCreate", event.role.asMention]}")
							.setFooter(locale["modules.eventLog.roleID", event.role.id], null)
							.setTimestamp(Instant.now())

						textChannel.sendMessageEmbeds(embed.build()).await()
					}
				}
			} catch (e: Exception) {
				logger.error(e) { "Erro ao criar cargo do event log" }
			}
		}
	}

	override fun onRoleDelete(event: RoleDeleteEvent) {
		if (DebugLog.cancelAllEvents)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
				val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
				val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
					loritta, ServerConfig::eventLogConfig
				) ?: return@launch

				if (eventLogConfig.enabled && eventLogConfig.roleDelete) {
					val textChannel = event.guild.getGuildMessageChannelById(
						eventLogConfig.roleDeleteLogChannelId ?: eventLogConfig.eventLogChannelId
					) ?: return@launch

					if (textChannel.canTalk() &&
						event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) &&
						event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {

						val embed = EmbedBuilder()
							.setColor(Color(221, 0, 0).rgb)
							.setDescription("\uD83D\uDDD1️ ${locale["modules.eventLog.roleDelete", event.role.name]}")
							.setFooter(locale["modules.eventLog.roleID", event.role.id], null)
							.setTimestamp(Instant.now())

						textChannel.sendMessageEmbeds(embed.build()).await()
					}
				}
			} catch (e: Exception) {
				logger.error(e) { "Erro ao deletar cargo do event log" }
			}
		}
	}

	override fun onRoleUpdateName(event: RoleUpdateNameEvent) {
		if (DebugLog.cancelAllEvents)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
				val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
				val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
					loritta, ServerConfig::eventLogConfig
				) ?: return@launch

				if (eventLogConfig.enabled && eventLogConfig.roleUpdate) {
					val textChannel = event.guild.getGuildMessageChannelById(
						eventLogConfig.roleUpdateLogChannelId ?: eventLogConfig.eventLogChannelId
					) ?: return@launch

					if (textChannel.canTalk() &&
						event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) &&
						event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {

						val embed = EmbedBuilder()
							.setColor(Color(238, 241, 0).rgb)
							.setDescription("\uD83D\uDCDD ${locale["modules.eventLog.roleUpdate", event.role.asMention]}\n\nName: ${event.oldName} → ${event.newName}")
							.setFooter(locale["modules.eventLog.roleID", event.role.id], null)
							.setTimestamp(Instant.now())

						textChannel.sendMessageEmbeds(embed.build()).await()
					}
				}
			} catch (e: Exception) {
				logger.error(e) { "Erro ao atualizar cargo do event log" }
			}
		}
	}

	override fun onChannelCreate(event: ChannelCreateEvent) {
		if (DebugLog.cancelAllEvents)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				val guild = event.channel.asGuildMessageChannel().guild
				val serverConfig = loritta.getOrCreateServerConfig(guild.idLong)
				val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
				val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
					loritta, ServerConfig::eventLogConfig
				) ?: return@launch

				if (eventLogConfig.enabled && eventLogConfig.channelCreate) {
					val textChannel = guild.getGuildMessageChannelById(
						eventLogConfig.channelCreateLogChannelId ?: eventLogConfig.eventLogChannelId
					) ?: return@launch

					if (textChannel.canTalk() &&
						guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) &&
						guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {

						val embed = EmbedBuilder()
							.setColor(Color(87, 242, 135).rgb)
							.setDescription("\uD83C\uDD95 ${locale["modules.eventLog.channelCreate", event.channel.name, event.channel.type.toString()]}")
							.setFooter(locale["modules.eventLog.channelID", event.channel.id], null)
							.setTimestamp(Instant.now())

						textChannel.sendMessageEmbeds(embed.build()).await()
					}
				}
			} catch (e: Exception) {
				logger.error(e) { "Erro ao criar canal do event log" }
			}
		}
	}

	override fun onChannelDelete(event: ChannelDeleteEvent) {
		if (DebugLog.cancelAllEvents)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				val guild = event.channel.asGuildMessageChannel().guild
				val serverConfig = loritta.getOrCreateServerConfig(guild.idLong)
				val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
				val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
					loritta, ServerConfig::eventLogConfig
				) ?: return@launch

				if (eventLogConfig.enabled && eventLogConfig.channelDelete) {
					val textChannel = guild.getGuildMessageChannelById(
						eventLogConfig.channelDeleteLogChannelId ?: eventLogConfig.eventLogChannelId
					) ?: return@launch

					if (textChannel.canTalk() &&
						guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) &&
						guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {

						val embed = EmbedBuilder()
							.setColor(Color(221, 0, 0).rgb)
							.setDescription("\uD83D\uDDD1️ ${locale["modules.eventLog.channelDelete", event.channel.name, event.channel.type.toString()]}")
							.setFooter(locale["modules.eventLog.channelID", event.channel.id], null)
							.setTimestamp(Instant.now())

						textChannel.sendMessageEmbeds(embed.build()).await()
					}
				}
			} catch (e: Exception) {
				logger.error(e) { "Erro ao deletar canal do event log" }
			}
		}
	}

	override fun onChannelUpdateName(event: ChannelUpdateNameEvent) {
		if (DebugLog.cancelAllEvents)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				val guild = event.channel.asGuildMessageChannel().guild
				val serverConfig = loritta.getOrCreateServerConfig(guild.idLong)
				val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
				val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
					loritta, ServerConfig::eventLogConfig
				) ?: return@launch

				if (eventLogConfig.enabled && eventLogConfig.channelUpdate) {
					val textChannel = guild.getGuildMessageChannelById(
						eventLogConfig.channelUpdateLogChannelId ?: eventLogConfig.eventLogChannelId
					) ?: return@launch

					if (textChannel.canTalk() &&
						guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) &&
						guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {

						val embed = EmbedBuilder()
							.setColor(Color(238, 241, 0).rgb)
							.setDescription("\uD83D\uDCDD ${locale["modules.eventLog.channelUpdate", event.channel.name]}\n\nName: ${event.oldValue} → ${event.newValue}")
							.setFooter(locale["modules.eventLog.channelID", event.channel.id], null)
							.setTimestamp(Instant.now())

						textChannel.sendMessageEmbeds(embed.build()).await()
					}
				}
			} catch (e: Exception) {
				logger.error(e) { "Erro ao atualizar canal do event log" }
			}
		}
	}

	override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
		if (DebugLog.cancelAllEvents)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
				val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
				val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
					loritta, ServerConfig::eventLogConfig
				) ?: return@launch

				val channelLeft = event.channelLeft
				val channelJoined = event.channelJoined

				if (channelJoined != null && channelLeft == null) {
					if (eventLogConfig.enabled && eventLogConfig.voiceChannelJoins) {
						val textChannel = event.guild.getGuildMessageChannelById(
							eventLogConfig.voiceChannelJoinsLogChannelId ?: eventLogConfig.eventLogChannelId
						) ?: return@launch

						if (!textChannel.canTalk() ||
							!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) ||
							!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
							return@launch

						val embed = EmbedBuilder()
							.setColor(Color(35, 209, 96).rgb)
							.setDescription("\uD83D\uDC49\uD83C\uDFA4 **${locale["modules.eventLog.joinedVoiceChannel", event.member.asMention, channelJoined.name]}**")
							.setAuthor("${event.member.user.name}#${event.member.user.discriminator}", null, event.member.user.effectiveAvatarUrl)
							.setFooter(locale["modules.eventLog.userID", event.member.user.id], null)
							.setTimestamp(Instant.now())

						textChannel.sendMessageEmbeds(embed.build()).await()
					}
				} else if (channelLeft != null && channelJoined == null) {
					if (eventLogConfig.enabled && eventLogConfig.voiceChannelLeaves) {
						val textChannel = event.guild.getGuildMessageChannelById(
							eventLogConfig.voiceChannelLeavesLogChannelId ?: eventLogConfig.eventLogChannelId
						) ?: return@launch

						if (!textChannel.canTalk() ||
							!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) ||
							!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
							return@launch

						val embed = EmbedBuilder()
							.setColor(Color(35, 209, 96).rgb)
							.setDescription("\uD83D\uDC48\uD83C\uDFA4 **${locale["modules.eventLog.leftVoiceChannel", event.member.asMention, channelLeft.name]}**")
							.setAuthor("${event.member.user.name}#${event.member.user.discriminator}", null, event.member.user.effectiveAvatarUrl)
							.setFooter(locale["modules.eventLog.userID", event.member.user.id], null)
							.setTimestamp(Instant.now())

						textChannel.sendMessageEmbeds(embed.build()).await()
					}
				} else if (channelLeft != null && channelJoined != null) {
					if (eventLogConfig.enabled && eventLogConfig.voiceChannelMove) {
						val textChannel = event.guild.getGuildMessageChannelById(
							eventLogConfig.voiceChannelMoveLogChannelId ?: eventLogConfig.eventLogChannelId
						) ?: return@launch

						if (!textChannel.canTalk() ||
							!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) ||
							!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
							return@launch

						val embed = EmbedBuilder()
							.setColor(Color(238, 241, 0).rgb)
							.setDescription("\uD83D\uDD04 ${locale["modules.eventLog.movedVoiceChannel", event.member.asMention, channelLeft.name, channelJoined.name]}")
							.setAuthor("${event.member.user.name}#${event.member.user.discriminator}", null, event.member.user.effectiveAvatarUrl)
							.setFooter(locale["modules.eventLog.userID", event.member.user.id], null)
							.setTimestamp(Instant.now())

						textChannel.sendMessageEmbeds(embed.build()).await()
					}
				}
			} catch (e: Exception) {
				logger.error(e) { "Erro ao processar evento de voz" }
			}
		}
	}

	override fun onGuildInviteCreate(event: GuildInviteCreateEvent) {
		if (DebugLog.cancelAllEvents)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
				val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
				val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
					loritta, ServerConfig::eventLogConfig
				) ?: return@launch

				if (eventLogConfig.enabled && eventLogConfig.inviteCreated) {
					val textChannel = event.guild.getGuildMessageChannelById(
						eventLogConfig.inviteCreatedLogChannelId ?: eventLogConfig.eventLogChannelId
					) ?: return@launch

					if (textChannel.canTalk() &&
						event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) &&
						event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {

						val inviter = event.invite.inviter
						val maxUses = event.invite.maxUses
						val maxAge = event.invite.maxAge

						val embed = EmbedBuilder()
							.setColor(Color(87, 242, 135).rgb)
							.setDescription(
								"\uD83D\uDCE8 ${locale["modules.eventLog.inviteCreated", inviter?.asMention ?: "Unknown", event.invite.code, event.invite.channel?.asMention ?: "Unknown", if (maxUses == 0) "∞" else maxUses.toString(), if (maxAge == 0) "∞" else "${maxAge / 3600}h"]}"
							)
							.setAuthor(
								"${inviter?.name ?: "Unknown"}#${inviter?.discriminator ?: "0000"}",
								null,
								inviter?.effectiveAvatarUrl
							)
							.setFooter(locale["modules.eventLog.userID", inviter?.id ?: "Unknown"], null)
							.setTimestamp(Instant.now())

						textChannel.sendMessageEmbeds(embed.build()).await()
					}
				}
			} catch (e: Exception) {
				logger.error(e) { "Erro ao criar convite do event log" }
			}
		}
	}
}.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
return@launch

val oldNickname = event.oldNickname ?: "\uD83E\uDD37 ${locale["modules.eventLog.noNickname"]}"
val newNickname = event.newNickname ?: "\uD83E\uDD37 ${locale["modules.eventLog.noNickname"]}"

val embed = EmbedBuilder()
	.setColor(Color(238, 241, 0).rgb)
	.setTimestamp(Instant.now())
	.setAuthor(
		"${event.member.user.name}#${event.member.user.discriminator}",
		null,
		event.member.user.effectiveAvatarUrl
	)
	.setDescription(
		"\uD83D\uDCDD ${
			locale.getList(
				"modules.eventLog.nicknameChanged",
				oldNickname,
				newNickname
			).joinToString("\n")
		}"
	)
	.setFooter(locale["modules.eventLog.userID", event.member.user.id], null)

textChannel.sendMessageEmbeds(embed.build()).await()
}
} catch (e: Exception) {
	logger.error(e) { "Erro ao processar mudança de nickname" }
}
}
}

override fun onGuildMemberUpdateTimeOut(event: GuildMemberUpdateTimeOutEvent) {
	if (DebugLog.cancelAllEvents)
		return

	GlobalScope.launch(loritta.coroutineDispatcher) {
		try {
			val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
			val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
				loritta, ServerConfig::eventLogConfig
			) ?: return@launch

			if (eventLogConfig.enabled && eventLogConfig.memberTimeout) {
				val textChannel = event.guild.getGuildMessageChannelById(
					eventLogConfig.memberTimeoutLogChannelId ?: eventLogConfig.eventLogChannelId
				) ?: return@launch

				if (textChannel.canTalk() &&
					event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) &&
					event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {

					val embed = EmbedBuilder()
						.setColor(Color(255, 165, 0).rgb)
						.setDescription(
							if (event.newTimeOutEnd != null) {
								"⏱️ ${locale["modules.eventLog.memberTimeout", event.member.asMention, event.newTimeOutEnd.toString()]}"
							} else {
								"⏱️ ${locale["modules.eventLog.memberTimeoutRemoved", event.member.asMention]}"
							}
						)
						.setAuthor(
							"${event.member.user.name}#${event.member.user.discriminator}",
							null,
							event.member.user.effectiveAvatarUrl
						)
						.setFooter(locale["modules.eventLog.userID", event.member.user.id], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao processar timeout de membro" }
		}
	}
}

override fun onGuildBan(event: GuildBanEvent) {
	if (DebugLog.cancelAllEvents)
		return

	bannedUsers.put("${event.guild.id}#${event.user.id}", true)

	GlobalScope.launch(loritta.coroutineDispatcher) {
		try {
			val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
			val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
				loritta, ServerConfig::eventLogConfig
			) ?: return@launch

			if (eventLogConfig.enabled && eventLogConfig.memberBanned) {
				val textChannel = event.guild.getGuildMessageChannelById(
					eventLogConfig.memberBannedLogChannelId ?: eventLogConfig.eventLogChannelId
				) ?: return@launch

				if (!textChannel.canTalk() ||
					!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) ||
					!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return@launch

				val embed = EmbedBuilder()
					.setTimestamp(Instant.now())
					.setColor(Color(221, 0, 0).rgb)
					.setAuthor(
						"${event.user.name}#${event.user.discriminator}",
						null,
						event.user.effectiveAvatarUrl
					)
					.setDescription("\uD83D\uDEAB **${locale["modules.eventLog.banned", event.user.name]}**")
					.setFooter(locale["modules.eventLog.userID", event.user.id], null)

				textChannel.sendMessageEmbeds(embed.build()).await()
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao processar ban" }
		}
	}
}

override fun onGuildUnban(event: GuildUnbanEvent) {
	if (DebugLog.cancelAllEvents)
		return

	GlobalScope.launch(loritta.coroutineDispatcher) {
		try {
			if (event.guild.idLong == Constants.PORTUGUESE_SUPPORT_GUILD_ID) {
				loritta.lorittaShards.getGuildById(Constants.ENGLISH_SUPPORT_GUILD_ID)
					?.unban(event.user)?.queue()
			}
			if (event.guild.idLong == Constants.ENGLISH_SUPPORT_GUILD_ID) {
				loritta.lorittaShards.getGuildById(Constants.PORTUGUESE_SUPPORT_GUILD_ID)
					?.unban(event.user)?.queue()
			}

			val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
			val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
				loritta, ServerConfig::eventLogConfig
			) ?: return@launch

			if (eventLogConfig.enabled && eventLogConfig.memberUnbanned) {
				val textChannel = event.guild.getGuildMessageChannelById(
					eventLogConfig.memberUnbannedLogChannelId ?: eventLogConfig.eventLogChannelId
				) ?: return@launch

				if (!textChannel.canTalk() ||
					!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) ||
					!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return@launch

				val embed = EmbedBuilder()
					.setTimestamp(Instant.now())
					.setColor(Color(35, 209, 96).rgb)
					.setAuthor(
						"${event.user.name}#${event.user.discriminator}",
						null,
						event.user.effectiveAvatarUrl
					)
					.setDescription("\uD83E\uDD1D **${locale["modules.eventLog.unbanned", event.user.name]}**")
					.setFooter(locale["modules.eventLog.userID", event.user.id], null)

				textChannel.sendMessageEmbeds(embed.build()).await()
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao processar unban" }
		}
	}
}

override fun onUserUpdateAvatar(event: UserUpdateAvatarEvent) {
	if (DebugLog.cancelAllEvents || event.user.isBot)
		return

	if (downloadedAvatarJobs[event.entity.id] != null)
		return

	downloadedAvatarJobs[event.entity.id] = GlobalScope.launch(loritta.coroutineDispatcher) {
		try {
			logger.info { "Baixando avatar de ${event.entity.id} para enviar no event log..." }

			val oldAvatarUrl = event.oldAvatarUrl?.replace("gif", "png") ?: event.user.defaultAvatarUrl
			val rawOldAvatar = LorittaUtils.downloadImage(loritta, oldAvatarUrl)
			val rawNewAvatar = LorittaUtils.downloadImage(loritta, event.user.getEffectiveAvatarUrl(ImageFormat.PNG))

			if (rawOldAvatar == null || rawNewAvatar == null) {
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
						val locale = loritta.localeManager.getLocaleById(it[ServerConfigs.localeId])
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
								.setTimestamp(Instant.now())
								.setAuthor("${event.user.name}#${event.user.discriminator}", null, event.user.effectiveAvatarUrl)
								.setColor(Constants.DISCORD_BLURPLE.rgb)
								.setImage("attachment://avatar.png")
								.setDescription("\uD83D\uDDBC ${locale["modules.eventLog.avatarChanged", event.user.asMention]}")
								.setFooter(locale["modules.eventLog.userID", event.user.id], null)

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
			logger.error(e) { "Erro ao fazer download do avatar de ${event.entity.id}" }
			downloadedAvatarJobs.remove(event.entity.id)
		}
	}
}

override fun onRoleCreate(event: RoleCreateEvent) {
	if (DebugLog.cancelAllEvents)
		return

	GlobalScope.launch(loritta.coroutineDispatcher) {
		try {
			val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
			val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
				loritta, ServerConfig::eventLogConfig
			) ?: return@launch

			if (eventLogConfig.enabled && eventLogConfig.roleCreate) {
				val textChannel = event.guild.getGuildMessageChannelById(
					eventLogConfig.roleCreateLogChannelId ?: eventLogConfig.eventLogChannelId
				) ?: return@launch

				if (textChannel.canTalk() &&
					event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) &&
					event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {

					val embed = EmbedBuilder()
						.setColor(Color(87, 242, 135).rgb)
						.setDescription("\uD83C\uDFF7️ ${locale["modules.eventLog.roleCreate", event.role.asMention]}")
						.setFooter(locale["modules.eventLog.roleID", event.role.id], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao criar cargo do event log" }
		}
	}
}

override fun onRoleDelete(event: RoleDeleteEvent) {
	if (DebugLog.cancelAllEvents)
		return

	GlobalScope.launch(loritta.coroutineDispatcher) {
		try {
			val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
			val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
				loritta, ServerConfig::eventLogConfig
			) ?: return@launch

			if (eventLogConfig.enabled && eventLogConfig.roleDelete) {
				val textChannel = event.guild.getGuildMessageChannelById(
					eventLogConfig.roleDeleteLogChannelId ?: eventLogConfig.eventLogChannelId
				) ?: return@launch

				if (textChannel.canTalk() &&
					event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) &&
					event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {

					val embed = EmbedBuilder()
						.setColor(Color(221, 0, 0).rgb)
						.setDescription("\uD83D\uDDD1️ ${locale["modules.eventLog.roleDelete", event.role.name]}")
						.setFooter(locale["modules.eventLog.roleID", event.role.id], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao deletar cargo do event log" }
		}
	}
}

override fun onRoleUpdateName(event: RoleUpdateNameEvent) {
	if (DebugLog.cancelAllEvents)
		return

	GlobalScope.launch(loritta.coroutineDispatcher) {
		try {
			val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
			val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
				loritta, ServerConfig::eventLogConfig
			) ?: return@launch

			if (eventLogConfig.enabled && eventLogConfig.roleUpdate) {
				val textChannel = event.guild.getGuildMessageChannelById(
					eventLogConfig.roleUpdateLogChannelId ?: eventLogConfig.eventLogChannelId
				) ?: return@launch

				if (textChannel.canTalk() &&
					event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) &&
					event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {

					val embed = EmbedBuilder()
						.setColor(Color(238, 241, 0).rgb)
						.setDescription("\uD83D\uDCDD ${locale["modules.eventLog.roleUpdate", event.role.asMention]}\n\nName: ${event.oldName} → ${event.newName}")
						.setFooter(locale["modules.eventLog.roleID", event.role.id], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao atualizar cargo do event log" }
		}
	}
}

override fun onChannelCreate(event: ChannelCreateEvent) {
	if (DebugLog.cancelAllEvents)
		return

	GlobalScope.launch(loritta.coroutineDispatcher) {
		try {
			val guild = event.channel.asGuildMessageChannel().guild
			val serverConfig = loritta.getOrCreateServerConfig(guild.idLong)
			val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
				loritta, ServerConfig::eventLogConfig
			) ?: return@launch

			if (eventLogConfig.enabled && eventLogConfig.channelCreate) {
				val textChannel = guild.getGuildMessageChannelById(
					eventLogConfig.channelCreateLogChannelId ?: eventLogConfig.eventLogChannelId
				) ?: return@launch

				if (textChannel.canTalk() &&
					guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) &&
					guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {

					val embed = EmbedBuilder()
						.setColor(Color(87, 242, 135).rgb)
						.setDescription("\uD83C\uDD95 ${locale["modules.eventLog.channelCreate", event.channel.name, event.channel.type.toString()]}")
						.setFooter(locale["modules.eventLog.channelID", event.channel.id], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao criar canal do event log" }
		}
	}
}

override fun onChannelDelete(event: ChannelDeleteEvent) {
	if (DebugLog.cancelAllEvents)
		return

	GlobalScope.launch(loritta.coroutineDispatcher) {
		try {
			val guild = event.channel.asGuildMessageChannel().guild
			val serverConfig = loritta.getOrCreateServerConfig(guild.idLong)
			val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
				loritta, ServerConfig::eventLogConfig
			) ?: return@launch

			if (eventLogConfig.enabled && eventLogConfig.channelDelete) {
				val textChannel = guild.getGuildMessageChannelById(
					eventLogConfig.channelDeleteLogChannelId ?: eventLogConfig.eventLogChannelId
				) ?: return@launch

				if (textChannel.canTalk() &&
					guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) &&
					guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {

					val embed = EmbedBuilder()
						.setColor(Color(221, 0, 0).rgb)
						.setDescription("\uD83D\uDDD1️ ${locale["modules.eventLog.channelDelete", event.channel.name, event.channel.type.toString()]}")
						.setFooter(locale["modules.eventLog.channelID", event.channel.id], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao deletar canal do event log" }
		}
	}
}

override fun onChannelUpdateName(event: ChannelUpdateNameEvent) {
	if (DebugLog.cancelAllEvents)
		return

	GlobalScope.launch(loritta.coroutineDispatcher) {
		try {
			val guild = event.channel.asGuildMessageChannel().guild
			val serverConfig = loritta.getOrCreateServerConfig(guild.idLong)
			val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
				loritta, ServerConfig::eventLogConfig
			) ?: return@launch

			if (eventLogConfig.enabled && eventLogConfig.channelUpdate) {
				val textChannel = guild.getGuildMessageChannelById(
					eventLogConfig.channelUpdateLogChannelId ?: eventLogConfig.eventLogChannelId
				) ?: return@launch

				if (textChannel.canTalk() &&
					guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) &&
					guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {

					val embed = EmbedBuilder()
						.setColor(Color(238, 241, 0).rgb)
						.setDescription("\uD83D\uDCDD ${locale["modules.eventLog.channelUpdate", event.channel.name]}\n\nName: ${event.oldValue} → ${event.newValue}")
						.setFooter(locale["modules.eventLog.channelID", event.channel.id], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao atualizar canal do event log" }
		}
	}
}

override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
	if (DebugLog.cancelAllEvents)
		return

	GlobalScope.launch(loritta.coroutineDispatcher) {
		try {
			val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
			val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
				loritta, ServerConfig::eventLogConfig
			) ?: return@launch

			val channelLeft = event.channelLeft
			val channelJoined = event.channelJoined

			if (channelJoined != null && channelLeft == null) {
				if (eventLogConfig.enabled && eventLogConfig.voiceChannelJoins) {
					val textChannel = event.guild.getGuildMessageChannelById(
						eventLogConfig.voiceChannelJoinsLogChannelId ?: eventLogConfig.eventLogChannelId
					) ?: return@launch

					if (!textChannel.canTalk() ||
						!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) ||
						!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
						return@launch

					val embed = EmbedBuilder()
						.setColor(Color(35, 209, 96).rgb)
						.setDescription("\uD83D\uDC49\uD83C\uDFA4 **${locale["modules.eventLog.joinedVoiceChannel", event.member.asMention, channelJoined.name]}**")
						.setAuthor("${event.member.user.name}#${event.member.user.discriminator}", null, event.member.user.effectiveAvatarUrl)
						.setFooter(locale["modules.eventLog.userID", event.member.user.id], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			} else if (channelLeft != null && channelJoined == null) {
				if (eventLogConfig.enabled && eventLogConfig.voiceChannelLeaves) {
					val textChannel = event.guild.getGuildMessageChannelById(
						eventLogConfig.voiceChannelLeavesLogChannelId ?: eventLogConfig.eventLogChannelId
					) ?: return@launch

					if (!textChannel.canTalk() ||
						!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) ||
						!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
						return@launch

					val embed = EmbedBuilder()
						.setColor(Color(35, 209, 96).rgb)
						.setDescription("\uD83D\uDC48\uD83C\uDFA4 **${locale["modules.eventLog.leftVoiceChannel", event.member.asMention, channelLeft.name]}**")
						.setAuthor("${event.member.user.name}#${event.member.user.discriminator}", null, event.member.user.effectiveAvatarUrl)
						.setFooter(locale["modules.eventLog.userID", event.member.user.id], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			} else if (channelLeft != null && channelJoined != null) {
				if (eventLogConfig.enabled && eventLogConfig.voiceChannelMove) {
					val textChannel = event.guild.getGuildMessageChannelById(
						eventLogConfig.voiceChannelMoveLogChannelId ?: eventLogConfig.eventLogChannelId
					) ?: return@launch

					if (!textChannel.canTalk() ||
						!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) ||
						!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
						return@launch

					val embed = EmbedBuilder()
						.setColor(Color(238, 241, 0).rgb)
						.setDescription("\uD83D\uDD04 ${locale["modules.eventLog.movedVoiceChannel", event.member.asMention, channelLeft.name, channelJoined.name]}")
						.setAuthor("${event.member.user.name}#${event.member.user.discriminator}", null, event.member.user.effectiveAvatarUrl)
						.setFooter(locale["modules.eventLog.userID", event.member.user.id], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao processar evento de voz" }
		}
	}
}

override fun onGuildInviteCreate(event: GuildInviteCreateEvent) {
	if (DebugLog.cancelAllEvents)
		return

	GlobalScope.launch(loritta.coroutineDispatcher) {
		try {
			val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
			val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(
				loritta, ServerConfig::eventLogConfig
			) ?: return@launch

			if (eventLogConfig.enabled && eventLogConfig.inviteCreated) {
				val textChannel = event.guild.getGuildMessageChannelById(
					eventLogConfig.inviteCreatedLogChannelId ?: eventLogConfig.eventLogChannelId
				) ?: return@launch

				if (textChannel.canTalk() &&
					event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) &&
					event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {

					val inviter = event.invite.inviter
					val maxUses = event.invite.maxUses
					val maxAge = event.invite.maxAge

					val embed = EmbedBuilder()
						.setColor(Color(87, 242, 135).rgb)
						.setDescription(
							"\uD83D\uDCE8 ${locale["modules.eventLog.inviteCreated", inviter?.asMention ?: "Unknown", event.invite.code, event.invite.channel?.asMention ?: "Unknown", if (maxUses == 0) "∞" else maxUses.toString(), if (maxAge == 0) "∞" else "${maxAge / 3600}h"]}"
						)
						.setAuthor(
							"${inviter?.name ?: "Unknown"}#${inviter?.discriminator ?: "0000"}",
							null,
							inviter?.effectiveAvatarUrl
						)
						.setFooter(locale["modules.eventLog.userID", inviter?.id ?: "Unknown"], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao criar convite do event log" }
		}
	}
}
}