package net.perfectdreams.loritta.morenitta.utils.eventlog

import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.dao.StoredMessage
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.EventLogConfig
import net.perfectdreams.loritta.morenitta.messageverify.LoriMessageDataUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import java.awt.Color
import java.time.Instant
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object EventLog {
	private val logger by HarmonyLoggerFactory.logger {}

	suspend fun onMessageReceived(loritta: LorittaBot, serverConfig: ServerConfig, message: Message) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && (eventLogConfig.messageDeleted || eventLogConfig.messageEdited)) {
				val savedMessage = LoriMessageDataUtils.convertMessageToSavedMessage(message)

				loritta.newSuspendedTransaction {
					StoredMessage.new(message.idLong) {
						this.authorId = message.author.idLong
						this.channelId = message.channel.idLong
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

	suspend fun onMessageUpdate(loritta: LorittaBot, serverConfig: ServerConfig, i18nContext: I18nContext, message: Message) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && (eventLogConfig.messageEdited || eventLogConfig.messageDeleted)) {
				val storedMessage = loritta.newSuspendedTransaction {
					StoredMessage.findById(message.idLong)
				}

				if (storedMessage != null) {
					val savedMessage = storedMessage.decryptContent(loritta)

					loritta.newSuspendedTransaction {
						storedMessage.encryptAndSetContent(loritta, LoriMessageDataUtils.convertMessageToSavedMessage(message))
					}

					val textChannel = message.guild.getGuildMessageChannelById(eventLogConfig.messageEditedLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return

					if (textChannel.canTalk() && message.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) && message.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL) && message.guild.selfMember.hasPermission(Permission.MESSAGE_ATTACH_FILES)) {
						if (savedMessage.content != message.contentRaw && eventLogConfig.messageEdited) {
							val embed = EmbedBuilder()
								.setColor(Color(238, 241, 0).rgb)
								.setDescription(
									"\uD83D\uDCDD ${
										i18nContext.get(
											I18nKeysData.Modules.EventLog.MessageEdited(
												memberMention = message.author.asMention,
												oldContent = savedMessage.content,
												newContent = message.contentRaw,
												channelMention = message.guildChannel.asMention
											)
										).joinToString("\n")
									}"
								)
								.setAuthor(
									"${message.author.name}#${message.author.discriminator}",
									null,
									message.author.effectiveAvatarUrl
								)
								.setFooter(i18nContext.get(I18nKeysData.Modules.EventLog.UserId(userId = message.author.id)), null)
								.setTimestamp(Instant.now())

							val fileName = LoriMessageDataUtils.createFileNameForSavedMessageImage(savedMessage)
							embed.setImage("attachment://$fileName")

							val finalImage =
								LoriMessageDataUtils.createSignedRenderedSavedMessage(loritta, savedMessage, true)

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

	suspend fun onVoiceJoin(loritta: LorittaBot, serverConfig: ServerConfig, member: Member, channelJoined: AudioChannelUnion) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.voiceChannelJoins) {
				val textChannel = member.guild.getGuildMessageChannelById(eventLogConfig.voiceChannelJoinsLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return
				val i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

				if (!textChannel.canTalk())
					return
				if (!member.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return
				if (!member.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return

				val embed = EmbedBuilder()
					.setColor(Color(35, 209, 96).rgb)
					.setDescription("\uD83D\uDC49\uD83C\uDFA4 **${i18nContext.get(I18nKeysData.Modules.EventLog.JoinedVoiceChannel(memberMention = member.asMention, channelName = channelJoined.name))}**")
					.setAuthor("${member.user.name}#${member.user.discriminator}", null, member.user.effectiveAvatarUrl)
					.setFooter(i18nContext.get(I18nKeysData.Modules.EventLog.UserId(userId = member.user.id)), null)
					.setTimestamp(Instant.now())
					.build()

				textChannel.sendMessageEmbeds(embed).await()
				return
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao entrar no canal de voz do event log" }
		}
	}

	suspend fun onVoiceLeave(loritta: LorittaBot, serverConfig: ServerConfig, member: Member, channelLeft: AudioChannelUnion) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.voiceChannelLeaves) {
				val textChannel = member.guild.getGuildMessageChannelById(eventLogConfig.voiceChannelLeavesLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return
				val i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)
				if (!textChannel.canTalk())
					return
				if (!member.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return
				if (!member.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return

				val embed = EmbedBuilder()
					.setColor(Color(35, 209, 96).rgb)
					.setDescription("\uD83D\uDC48\uD83C\uDFA4 **${i18nContext.get(I18nKeysData.Modules.EventLog.LeftVoiceChannel(memberMention = member.asMention, channelName = channelLeft.name))}**")
					.setAuthor("${member.user.name}#${member.user.discriminator}", null, member.user.effectiveAvatarUrl)
					.setFooter(i18nContext.get(I18nKeysData.Modules.EventLog.UserId(userId = member.user.id)), null)
					.setTimestamp(Instant.now())
					.build()

				textChannel.sendMessageEmbeds(embed).await()
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao sair do canal de voz do event log" }
		}
	}

	private fun generateRandomInitVector(loritta: LorittaBot) = ByteArray(16).apply {
		loritta.random.nextBytes(this)
	}

	fun encryptMessage(loritta: LorittaBot, content: String): EncryptedMessage {
		val initVector = generateRandomInitVector(loritta)

		val iv = IvParameterSpec(initVector)
		val skeySpec = SecretKeySpec(loritta.config.loritta.messageEncryption.encryptionKey.toByteArray(charset("UTF-8")), "AES")

		val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv)
		val encrypted = cipher.doFinal(content.toByteArray())
		return EncryptedMessage(Base64.getEncoder().encodeToString(initVector), Base64.getEncoder().encodeToString(encrypted))
	}

	fun decryptMessage(loritta: LorittaBot, initVector: String, encryptedContent: String): String {
		val iv = IvParameterSpec(Base64.getDecoder().decode(initVector))
		val skeySpec = SecretKeySpec(loritta.config.loritta.messageEncryption.encryptionKey.toByteArray(charset("UTF-8")), "AES")

		val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv)
		val original = cipher.doFinal(Base64.getDecoder().decode(encryptedContent))

		return String(original)
	}

	data class EncryptedMessage(
		val initializationVector: String,
		val encryptedMessage: String
	)
}