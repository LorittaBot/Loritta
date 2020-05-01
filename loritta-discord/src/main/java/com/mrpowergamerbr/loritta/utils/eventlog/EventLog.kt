package com.mrpowergamerbr.loritta.utils.eventlog

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.dao.StoredMessage
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.parallax.wrappers.ParallaxEmbed
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.getTextChannelByNullableId
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.webhook.DiscordMessage
import com.mrpowergamerbr.loritta.utils.webhook.DiscordWebhook
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
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
			.build<Long, DiscordWebhook>()
			.asMap()

	suspend fun getOrCreateEventLogWebhook(guild: Guild, serverConfig: MongoServerConfig): DiscordWebhook? {
		val eventLogConfig = serverConfig.eventLogConfig
		val channel = guild.getTextChannelByNullableId(eventLogConfig.eventLogChannelId) ?: return null

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
			val loriWebhook = DiscordWebhook(
					firstWebhook.url,
					loritta.http,
					loritta.coroutineDispatcher
			)

			cachedEventLogWebhooks[channel.idLong] = loriWebhook
			return loriWebhook
		}

		// Mas se não tiver, crie uma nova webhook
		val newWebhook = channel.createWebhook("Loritta (Event Log)")
				.await()

		val loriWebhook = DiscordWebhook(
				newWebhook.url,
				loritta.http,
				loritta.coroutineDispatcher
		)

		cachedEventLogWebhooks[channel.idLong] = loriWebhook
		return loriWebhook
	}

	fun onMessageReceived(serverConfig: MongoServerConfig, message: Message) {
		try {
			val eventLogConfig = serverConfig.eventLogConfig

			if (eventLogConfig.isEnabled && (eventLogConfig.messageDeleted || eventLogConfig.messageEdit)) {
				val attachments = mutableListOf<String>()

				message.attachments.forEach {
					// https://i.imgur.com/VyVlzVe.png
					attachments.add(it.url.replace("cdn.discordapp.com", "media.discordapp.net"))
				}

				transaction(Databases.loritta) {
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

	suspend fun onMessageUpdate(serverConfig: MongoServerConfig, locale: LegacyBaseLocale, message: Message) {
		val eventLogConfig = serverConfig.eventLogConfig

		try {
			if (eventLogConfig.isEnabled && (eventLogConfig.messageEdit || eventLogConfig.messageDeleted)) {
				val textChannel = message.guild.getTextChannelByNullableId(eventLogConfig.eventLogChannelId)
				if (textChannel != null && textChannel.canTalk()) {
					if (!message.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
						return
					if (!message.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
						return
					if (!message.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
						return

					val storedMessage = transaction(Databases.loritta) {
						StoredMessage.findById(message.idLong)
					}

					if (storedMessage != null && storedMessage.content != message.contentRaw && eventLogConfig.messageEdit) {
						val webhook = getOrCreateEventLogWebhook(message.guild, serverConfig)
						if (webhook != null) {
							val embed = ParallaxEmbed()
							// embed.setTimestamp(Instant.now())

							embed.setColor(Color(238, 241, 0))

							embed.setAuthor("${message.member?.user?.name}#${message.member?.user?.discriminator}", null, message.member?.user?.effectiveAvatarUrl)
							embed.setDescription("\uD83D\uDCDD ${locale["EVENTLOG_MESSAGE_EDITED", message.member?.asMention, storedMessage.content, message.contentRaw, message.textChannel.asMention]}")
							embed.setFooter(locale["EVENTLOG_USER_ID", message.member?.user?.id], null)

							webhook.send(
									DiscordMessage(
											message.guild.selfMember.user.name,
											" ",
											message.guild.selfMember.user.effectiveAvatarUrl,
											listOf(
													embed
											)
									)
							)
						}
					}

					if (storedMessage != null) {
						transaction(Databases.loritta) {
							storedMessage.content = message.contentRaw
						}
					}
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao atualizar mensagem do event log" }
		}
	}

	suspend fun onVoiceJoin(serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, member: Member, channelJoined: VoiceChannel) {
		try {
			val eventLogConfig = legacyServerConfig.eventLogConfig

			if (eventLogConfig.isEnabled && eventLogConfig.voiceChannelJoins) {
				val textChannel = member.guild.getTextChannelByNullableId(eventLogConfig.eventLogChannelId) ?: return
				val locale = loritta.getLegacyLocaleById(serverConfig.localeId)

				if (!textChannel.canTalk())
					return
				if (!member.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return
				if (!member.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return
				if (!member.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
					return

				val webhook = getOrCreateEventLogWebhook(channelJoined.guild, legacyServerConfig) ?: return

				val embed = ParallaxEmbed()
				// embed.setTimestamp(Instant.now())

				embed.setColor(Color(35, 209, 96))

				embed.setAuthor("${member.user.name}#${member.user.discriminator}", null, member.user.effectiveAvatarUrl)
				embed.setDescription("\uD83D\uDC49\uD83C\uDFA4 **${locale["EVENTLOG_JoinedVoiceChannel", member.asMention, channelJoined.name]}**")
				embed.setFooter(locale["EVENTLOG_USER_ID", member.user.id], null)

				webhook.send(
						DiscordMessage(
								channelJoined.guild.selfMember.user.name,
								" ",
								channelJoined.guild.selfMember.user.effectiveAvatarUrl,
								listOf(
										embed
								)
						)
				)
				return
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao entrar no canal de voz do event log" }
		}
	}

	suspend fun onVoiceLeave(serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, member: Member, channelLeft: VoiceChannel) {
		try {
			val eventLogConfig = legacyServerConfig.eventLogConfig

			if (eventLogConfig.isEnabled && eventLogConfig.voiceChannelLeaves) {
				val textChannel = member.guild.getTextChannelByNullableId(eventLogConfig.eventLogChannelId) ?: return
				val locale = loritta.getLegacyLocaleById(serverConfig.localeId)
				if (!textChannel.canTalk())
					return
				if (!member.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return
				if (!member.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return
				if (!member.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
					return

				val webhook = getOrCreateEventLogWebhook(channelLeft.guild, legacyServerConfig) ?: return

				val embed = ParallaxEmbed()
				// embed.setTimestamp(Instant.now())
				embed.setColor(Color(35, 209, 96))

				embed.setAuthor("${member.user.name}#${member.user.discriminator}", null, member.user.effectiveAvatarUrl)
				embed.setDescription("\uD83D\uDC48\uD83C\uDFA4 **${locale["EVENTLOG_LeftVoiceChannel", member.asMention, channelLeft.name]}**")
				embed.setFooter(locale["EVENTLOG_USER_ID", member.user.id], null)

				webhook.send(
						DiscordMessage(
								channelLeft.guild.selfMember.user.name,
								" ",
								channelLeft.guild.selfMember.user.effectiveAvatarUrl,
								listOf(
										embed
								)
						)
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