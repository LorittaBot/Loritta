package com.mrpowergamerbr.loritta.utils.eventlog

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.WebhookClientBuilder
import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.dao.StoredMessage
import com.mrpowergamerbr.loritta.utils.extensions.await
import net.perfectdreams.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.perfectdreams.loritta.dao.servers.moduleconfigs.EventLogConfig
import java.awt.Color
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object EventLog {
	val logger = KotlinLogging.logger {}
	val cachedEventLogWebhooks = Caffeine.newBuilder()
			.expireAfterAccess(1L, TimeUnit.DAYS)
			.maximumSize(10_000)
			.build<Long, WebhookClient>()
			.asMap()

	suspend fun getOrCreateEventLogWebhook(guild: Guild, eventLogConfig: EventLogConfig): WebhookClient? {
		val channel = guild.getTextChannelById(eventLogConfig.eventLogChannelId) ?: return null

		val webhook = cachedEventLogWebhooks[channel.idLong]

		if (webhook != null)
			return webhook

		if (!guild.selfMember.hasPermission(channel, Permission.MANAGE_WEBHOOKS))
			return null

		val webhooks = channel.retrieveWebhooks().await()
				.filter {
					// Webhooks criadas pelo usuário são INCOMING
					it.type == WebhookType.INCOMING
				}

		// Reutilizar webhook já criada
		if (webhooks.isNotEmpty()) {
			val firstWebhook = webhooks.first()
			val loriWebhook = WebhookClientBuilder(firstWebhook.url)
					.setExecutorService(loritta.webhookExecutor)
					.setHttpClient(loritta.webhookOkHttpClient)
					.build()

			cachedEventLogWebhooks[channel.idLong] = loriWebhook
			return loriWebhook
		}

		// Mas se não tiver, crie uma nova webhook
		val newWebhook = channel.createWebhook("Loritta (Event Log)")
				.await()

		val loriWebhook = WebhookClientBuilder(newWebhook.url)
				.setExecutorService(loritta.webhookExecutor)
				.setHttpClient(loritta.webhookOkHttpClient)
				.build()

		cachedEventLogWebhooks[channel.idLong] = loriWebhook
		return loriWebhook
	}

	suspend fun onMessageReceived(serverConfig: ServerConfig, message: Message) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && (eventLogConfig.messageDeleted || eventLogConfig.messageEdited)) {
				val attachments = mutableListOf<String>()

				message.attachments.forEach {
					// https://i.imgur.com/VyVlzVe.png
					attachments.add(it.url.replace("cdn.discordapp.com", "media.discordapp.net"))
				}

				loritta.newSuspendedTransaction {
					StoredMessage.new(message.idLong) {
						authorId = message.author.idLong
						channelId = message.channel.idLong
						content = message.contentRaw
						createdAt = System.currentTimeMillis()
						storedAttachments = attachments.toTypedArray()
					}
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao salvar mensagem do event log" }
		}
	}

	suspend fun onMessageUpdate(serverConfig: ServerConfig, locale: BaseLocale, message: Message) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && (eventLogConfig.messageEdited || eventLogConfig.messageDeleted)) {
				val textChannel = message.guild.getTextChannelById(eventLogConfig.eventLogChannelId) ?: return

				if (textChannel.canTalk()) {
					if (!message.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
						return
					if (!message.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
						return
					if (!message.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
						return

					val storedMessage = loritta.newSuspendedTransaction {
						StoredMessage.findById(message.idLong)
					}

					if (storedMessage != null && storedMessage.content != message.contentRaw && eventLogConfig.messageEdited) {
						val webhook = getOrCreateEventLogWebhook(message.guild, eventLogConfig)
						if (webhook != null) {
							val embed = WebhookEmbedBuilder()
									.setColor(Color(238, 241, 0).rgb)
									.setDescription("\uD83D\uDCDD ${locale.getList("modules.eventLog.messageEdited", message.member?.asMention, storedMessage.content, message.contentRaw, message.textChannel.asMention).joinToString("\n")}")
									.setAuthor(WebhookEmbed.EmbedAuthor("${message.member?.user?.name}#${message.member?.user?.discriminator}", null, message.member?.user?.effectiveAvatarUrl))
									.setFooter(WebhookEmbed.EmbedFooter(locale["modules.eventLog.userID", message.member?.user?.id], null))
									.setTimestamp(Instant.now())

							webhook.send(
									WebhookMessageBuilder()
											.setUsername(message.guild.selfMember.user.name)
											.setAvatarUrl(message.guild.selfMember.user.effectiveAvatarUrl)
											.setContent(" ")
											.addEmbeds(embed.build())
											.build()
							)
						}
					}

					if (storedMessage != null) {
						loritta.newSuspendedTransaction {
							storedMessage.content = message.contentRaw
						}
					}
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao atualizar mensagem do event log" }
		}
	}

	suspend fun onVoiceJoin(serverConfig: ServerConfig, member: Member, channelJoined: VoiceChannel) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.voiceChannelJoins) {
				val textChannel = member.guild.getTextChannelById(eventLogConfig.eventLogChannelId) ?: return
				val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)

				if (!textChannel.canTalk())
					return
				if (!member.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return
				if (!member.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return
				if (!member.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
					return

				val webhook = getOrCreateEventLogWebhook(channelJoined.guild, eventLogConfig) ?: return

				val embed = WebhookEmbedBuilder()
						.setColor(Color(35, 209, 96).rgb)
						.setDescription("\uD83D\uDC49\uD83C\uDFA4 **${locale["modules.eventLog.joinedVoiceChannel", member.asMention, channelJoined.name]}**")
						.setAuthor(WebhookEmbed.EmbedAuthor("${member.user.name}#${member.user.discriminator}", null, member.user.effectiveAvatarUrl))
						.setFooter(WebhookEmbed.EmbedFooter(locale["modules.eventLog.userID", member.user.id], null))
						.setTimestamp(Instant.now())

				webhook.send(
						WebhookMessageBuilder()
								.setUsername(member.guild.selfMember.user.name)
								.setAvatarUrl(member.guild.selfMember.user.effectiveAvatarUrl)
								.setContent(" ")
								.addEmbeds(embed.build())
								.build()
				)
				return
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao entrar no canal de voz do event log" }
		}
	}

	suspend fun onVoiceLeave(serverConfig: ServerConfig, member: Member, channelLeft: VoiceChannel) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.voiceChannelLeaves) {
				val textChannel = member.guild.getTextChannelById(eventLogConfig.eventLogChannelId) ?: return
				val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
				if (!textChannel.canTalk())
					return
				if (!member.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return
				if (!member.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return
				if (!member.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
					return

				val webhook = getOrCreateEventLogWebhook(channelLeft.guild, eventLogConfig) ?: return

				val embed = WebhookEmbedBuilder()
						.setColor(Color(35, 209, 96).rgb)
						.setDescription("\uD83D\uDC48\uD83C\uDFA4 **${locale["modules.eventLog.leftVoiceChannel", member.asMention, channelLeft.name]}**")
						.setAuthor(WebhookEmbed.EmbedAuthor("${member.user.name}#${member.user.discriminator}", null, member.user.effectiveAvatarUrl))
						.setFooter(WebhookEmbed.EmbedFooter(locale["modules.eventLog.userID", member.user.id], null))
						.setTimestamp(Instant.now())

				webhook.send(
						WebhookMessageBuilder()
								.setUsername(member.guild.selfMember.user.name)
								.setAvatarUrl(member.guild.selfMember.user.effectiveAvatarUrl)
								.setContent(" ")
								.addEmbeds(embed.build())
								.build()
				)
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao sair do canal de voz do event log" }
		}
	}

	fun generateRandomInitVector() = ByteArray(16).apply {
		loritta.random.nextBytes(this)
	}

	fun encryptMessage(content: String): EncryptedMessage {
		val initVector = generateRandomInitVector()

		val iv = IvParameterSpec(initVector)
		val skeySpec = SecretKeySpec(loritta.discordConfig.messageEncryption.encryptionKey.toByteArray(charset("UTF-8")), "AES")

		val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv)
		val encrypted = cipher.doFinal(content.toByteArray())
		return EncryptedMessage(Base64.getEncoder().encodeToString(initVector), Base64.getEncoder().encodeToString(encrypted))
	}

	fun decryptMessage(initVector: String, encryptedContent: String): String {
		val iv = IvParameterSpec(Base64.getDecoder().decode(initVector))
		val skeySpec = SecretKeySpec(loritta.discordConfig.messageEncryption.encryptionKey.toByteArray(charset("UTF-8")), "AES")

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