package net.perfectdreams.loritta.morenitta.utils.eventlog

import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.loritta.common.locale.BaseLocale
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
	private val logger = KotlinLogging.logger {}

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

	suspend fun onMessageUpdate(loritta: LorittaBot, serverConfig: ServerConfig, locale: BaseLocale, message: Message) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && (eventLogConfig.messageEdited || eventLogConfig.messageDeleted)) {
				val textChannel = message.guild.getGuildMessageChannelById(eventLogConfig.messageEditedLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return

				if (textChannel.canTalk()) {
					if (!message.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
						return
					if (!message.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
						return

					val storedMessage = loritta.newSuspendedTransaction {
						StoredMessage.findById(message.idLong)
					}

					if (storedMessage != null && storedMessage.decryptContent(loritta).content != message.contentRaw && eventLogConfig.messageEdited) {
						val savedMessage = storedMessage.decryptContent(loritta)
						val embed = EmbedBuilder()
							.setColor(Color(238, 241, 0).rgb)
							.setDescription("\uD83D\uDCDD ${locale.getList("modules.eventLog.messageEdited", message.member?.asMention, savedMessage.content, message.contentRaw, message.guildChannel.asMention).joinToString("\n")}")
							.setAuthor("${message.member?.user?.name}#${message.member?.user?.discriminator}", null, message.member?.user?.effectiveAvatarUrl)
							.setFooter(locale["modules.eventLog.userID", message.member?.user?.id], null)
							.setTimestamp(Instant.now())

						val fileName = LoriMessageDataUtils.createFileNameForSavedMessageImage(savedMessage)
						embed.setImage("attachment://$fileName")

						val finalImage = LoriMessageDataUtils.createSignedRenderedSavedMessage(loritta, savedMessage, true)

						textChannel.sendMessageEmbeds(embed.build())
							.addFiles(FileUpload.fromData(finalImage, fileName))
							.await()
					}

					if (storedMessage != null) {
						loritta.newSuspendedTransaction {
							storedMessage.encryptAndSetContent(loritta, LoriMessageDataUtils.convertMessageToSavedMessage(message))
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
				val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)

				if (!textChannel.canTalk())
					return
				if (!member.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return
				if (!member.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return

				val embed = EmbedBuilder()
					.setColor(Color(35, 209, 96).rgb)
					.setDescription("\uD83D\uDC49\uD83C\uDFA4 **${locale["modules.eventLog.joinedVoiceChannel", member.asMention, channelJoined.name]}**")
					.setAuthor("${member.user.name}#${member.user.discriminator}", null, member.user.effectiveAvatarUrl)
					.setFooter(locale["modules.eventLog.userID", member.user.id], null)
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
				val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
				if (!textChannel.canTalk())
					return
				if (!member.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return
				if (!member.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return

				val embed = EmbedBuilder()
					.setColor(Color(35, 209, 96).rgb)
					.setDescription("\uD83D\uDC48\uD83C\uDFA4 **${locale["modules.eventLog.leftVoiceChannel", member.asMention, channelLeft.name]}**")
					.setAuthor("${member.user.name}#${member.user.discriminator}", null, member.user.effectiveAvatarUrl)
					.setFooter(locale["modules.eventLog.userID", member.user.id], null)
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