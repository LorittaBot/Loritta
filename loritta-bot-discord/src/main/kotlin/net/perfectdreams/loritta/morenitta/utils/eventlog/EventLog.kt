package net.perfectdreams.loritta.morenitta.utils.eventlog

import club.minnced.discord.webhook.WebhookClientBuilder
import club.minnced.discord.webhook.exception.HttpException
import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import club.minnced.discord.webhook.send.WebhookMessage
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion
import net.perfectdreams.loritta.common.exposed.dao.CachedDiscordWebhook
import net.perfectdreams.loritta.common.exposed.tables.CachedDiscordWebhooks
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.webhooks.WebhookState
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.dao.StoredMessage
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.EventLogConfig
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import org.jetbrains.exposed.sql.upsert
import org.json.JSONException
import java.awt.Color
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

object EventLog {
	@OptIn(ExperimentalTime::class)
	private val MISSING_PERMISSIONS_COOLDOWN = 15.0.toDuration(DurationUnit.MINUTES)

	@OptIn(ExperimentalTime::class)
	private val UNKNOWN_WEBHOOK_PHASE_2_COOLDOWN = 15.0.toDuration(DurationUnit.MINUTES)

	val logger = KotlinLogging.logger {}
	// This is used to avoid spamming Discord with the same webhook creation request, so we synchronize access to the method
	private val webhookRetrievalMutex = Caffeine.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build<Long, Mutex>()
		.asMap()

	/**
	 * Sends the [message] to the [channelId] via a webhook, the webhook may be created or pulled from the guild if it is needed
	 *
	 * @param applicationId used as a webhook filter, will only get webhooks created by the application ID
	 * @param channelId     where the message will be sent
	 * @param message       the message that will be sent
	 * @return if the message was successfully sent
	 */
	@OptIn(ExperimentalTime::class)
	suspend fun sendMessageInEventLogViaWebhook(loritta: LorittaBot, message: WebhookMessage, guild: Guild, eventLogConfig: EventLogConfig): Boolean {
		// From SocialRelayer, changed a bit to use JDA
		val channel = guild.getTextChannelById(eventLogConfig.eventLogChannelId) ?: return false
		val channelId = channel.idLong

		val mutex = webhookRetrievalMutex.getOrPut(channelId) { Mutex() }
		logger.info { "Retrieving webhook to be used in $channelId, is mutex locked? ${mutex.isLocked}" }
		return mutex.withLock {
			_sendMessageInEventLogViaWebhook(loritta, message, guild, channel)
		}
	}

	/**
	 * Sends the [message] to the [channelId] via a webhook, the webhook may be created or pulled from the guild if it is needed
	 *
	 * @param applicationId used as a webhook filter, will only get webhooks created by the application ID
	 * @param channelId where the message will be sent
	 * @param message   the message that will be sent
	 * @return if the message was successfully sent
	 */
	@OptIn(ExperimentalTime::class)
	// This is doesn't wrap in a mutex, that's why it is private
	// The reason it starts with a underscore is because it is a private method and I can't find another good name for it
	// The reason this is a separate method is to avoid deadlocking due to accessing an already locked mutex when trying to retrieve the webhook
	private suspend fun _sendMessageInEventLogViaWebhook(loritta: LorittaBot, message: WebhookMessage, guild: Guild, channel: TextChannel): Boolean {
		// From SocialRelayer, changed a bit to use JDA
		val channelId = channel.idLong

		logger.info { "Trying to retrieve a webhook to be used in $channelId" }

		val alreadyCachedWebhookFromDatabase = withContext(Dispatchers.IO) {
			loritta.pudding.transaction {
				CachedDiscordWebhook.findById(channelId)
			}
		}

		if (alreadyCachedWebhookFromDatabase != null) {
			val shouldIgnoreDueToUnknownChannel = alreadyCachedWebhookFromDatabase.state == WebhookState.UNKNOWN_CHANNEL
			val shouldIgnoreDueToMissingPermissions = alreadyCachedWebhookFromDatabase.state == WebhookState.MISSING_PERMISSION && MISSING_PERMISSIONS_COOLDOWN.toLong(DurationUnit.MILLISECONDS) >= (System.currentTimeMillis() - alreadyCachedWebhookFromDatabase.updatedAt)
			val shouldIgnoreDueToUnknownWebhookPhaseTwo = alreadyCachedWebhookFromDatabase.state == WebhookState.UNKNOWN_WEBHOOK_PHASE_2 && UNKNOWN_WEBHOOK_PHASE_2_COOLDOWN.toLong(DurationUnit.MILLISECONDS) >= (System.currentTimeMillis() - alreadyCachedWebhookFromDatabase.updatedAt)
			val shouldIgnore = shouldIgnoreDueToUnknownChannel || shouldIgnoreDueToMissingPermissions || shouldIgnoreDueToUnknownWebhookPhaseTwo

			if (shouldIgnore) {
				logger.warn { "Ignoring webhook retrieval for $channelId because I wasn't able to create a webhook for it before... Webhook State: ${alreadyCachedWebhookFromDatabase.state}" }
				return false
			}
		}

		var guildWebhookFromDatabase = alreadyCachedWebhookFromDatabase

		// Okay, so we don't have any webhooks available OR the last time we tried checking it, it wasn't "SUCCESS"... let's try pulling them from Discord and then register them!
		if (guildWebhookFromDatabase == null || guildWebhookFromDatabase.state != WebhookState.SUCCESS) {
			logger.info { "First available webhook of $channelId to send a message is missing, trying to pull webhooks from the channel..." }

			try {
				if (!guild.selfMember.hasPermission(channel, Permission.MANAGE_WEBHOOKS)) {
					logger.warn { "Failed to get webhook in channel $channelId due to lack of manage webhooks permission" }

					withContext(Dispatchers.IO) {
						loritta.pudding.transaction {
							CachedDiscordWebhooks.upsert(CachedDiscordWebhooks.id) {
								it[id] = channelId
								// We don't replace the webhook token here... there is no pointing in replacing it.
								it[state] = WebhookState.MISSING_PERMISSION
								it[updatedAt] = System.currentTimeMillis()
							}.resultedValues!!.first()
						}
					}
					return false
				}

				// Try pulling the already created webhooks...
				val webhooks = channel.retrieveWebhooks().await()

				// Webhooks created by users or bots are INCOMING and we only want to get webhooks created by Loritta!
				// See: https://github.com/discord/discord-api-docs/issues/3056
				val firstAvailableWebhook = webhooks.firstOrNull { it.type == WebhookType.INCOMING && it.ownerAsUser?.idLong == guild.selfMember.idLong }
				var createdWebhook: Webhook? = null

				// Oh no, there isn't any webhooks available, let's create one!
				if (firstAvailableWebhook == null) {
					logger.info { "No available webhooks in $channelId to send the message, creating a new webhook..." }

					val jdaWebhook = channel.createWebhook("Loritta (Event Log)")
						.await()

					createdWebhook = jdaWebhook
				}

				val webhook = createdWebhook ?: firstAvailableWebhook ?: error("No webhook was found!")

				logger.info { "Successfully found webhook in $channelId!" }

				// Store the newly found webhook in our database!
				guildWebhookFromDatabase = withContext(Dispatchers.IO) {
					loritta.pudding.transaction {
						CachedDiscordWebhook.wrapRow(
							CachedDiscordWebhooks.upsert(CachedDiscordWebhooks.id) {
								it[id] = channelId
								it[webhookId] = webhook.idLong
								it[webhookToken] = webhook.token!! // I doubt that the token can be null so let's just force null, heh
								it[state] = WebhookState.SUCCESS
								it[updatedAt] = System.currentTimeMillis()
							}.resultedValues!!.first()
						)
					}
				}
			} catch (e: Exception) {
				logger.warn(e) { "Failed to get webhook in channel $channelId" }

				withContext(Dispatchers.IO) {
					loritta.pudding.transaction {
						CachedDiscordWebhooks.upsert(CachedDiscordWebhooks.id) {
							it[id] = channelId
							// We don't replace the webhook token here... there is no pointing in replacing it.
							it[state] = WebhookState.MISSING_PERMISSION
							it[updatedAt] = System.currentTimeMillis()
						}.resultedValues!!.first()
					}
				}
				return false
			}
		}

		val webhook = guildWebhookFromDatabase

		logger.info { "Sending $message in $channelId... Using webhook $webhook" }

		try {
			withContext(Dispatchers.IO) {
				WebhookClientBuilder("https://discord.com/api/webhooks/${webhook.webhookId}/${webhook.webhookToken}")
					.setExecutorService(loritta.webhookExecutor)
					.setHttpClient(loritta.webhookOkHttpClient)
					.setWait(true) // We want to wait to check if the webhook still exists!
					.build()
					.send(message)
					.await()
			}
		} catch (e: JSONException) {
			// Workaround for https://github.com/MinnDevelopment/discord-webhooks/issues/34
			// Please remove this later!
		} catch (e: HttpException) {
			val statusCode = e.code

			return if (statusCode == 404) {
				// So, this actually depends on what was the last phase
				//
				// Some DUMB people love using bots that automatically delete webhooks to avoid "users abusing them"... well why not manage your permissions correctly then?
				// So we have two phases: Phase 1 and Phase 2
				// Phase 1 means that the webhook should be retrieved again without any issues
				// Phase 2 means that SOMEONE is deleting the bot so it should just be ignored for a few minutes
				logger.warn(e) { "Webhook $webhook in $channelId does not exist! Current state is ${webhook.state}..." }
				withContext(Dispatchers.IO) {
					loritta.pudding.transaction {
						webhook.state = if (webhook.state == WebhookState.UNKNOWN_WEBHOOK_PHASE_1) WebhookState.UNKNOWN_WEBHOOK_PHASE_2 else WebhookState.UNKNOWN_WEBHOOK_PHASE_1
						webhook.updatedAt = System.currentTimeMillis()
					}
				}
				logger.warn(e) { "Webhook $webhook in $channelId does not exist and its state was updated to ${webhook.state}!" }

				_sendMessageInEventLogViaWebhook(loritta, message, guild, channel)
			} else {
				logger.warn(e) { "Something went wrong while sending the webhook message $message in $channelId using webhook $webhook!" }
				false
			}
		}

		logger.info { "Everything went well when sending $message in $channelId using webhook $webhook, updating last used time..." }

		withContext(Dispatchers.IO) {
			loritta.pudding.transaction {
				webhook.lastSuccessfullyExecutedAt = System.currentTimeMillis()
			}
		}

		return true // yay! :smol_gessy:
	}

	suspend fun onMessageReceived(loritta: LorittaBot, serverConfig: ServerConfig, message: Message) {
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
						createdAt = System.currentTimeMillis()
						storedAttachments = attachments
						this.encryptAndSetContent(loritta, message.contentRaw)
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
				val textChannel = message.guild.getGuildMessageChannelById(eventLogConfig.eventLogChannelId) ?: return

				if (textChannel.canTalk()) {
					if (!message.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
						return
					if (!message.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
						return

					val storedMessage = loritta.newSuspendedTransaction {
						StoredMessage.findById(message.idLong)
					}

					if (storedMessage != null && storedMessage.decryptContent(loritta) != message.contentRaw && eventLogConfig.messageEdited) {
						val embed = WebhookEmbedBuilder()
							.setColor(Color(238, 241, 0).rgb)
							.setDescription("\uD83D\uDCDD ${locale.getList("modules.eventLog.messageEdited", message.member?.asMention, storedMessage.decryptContent(loritta), message.contentRaw, message.guildChannel.asMention).joinToString("\n")}")
							.setAuthor(WebhookEmbed.EmbedAuthor("${message.member?.user?.name}#${message.member?.user?.discriminator}", null, message.member?.user?.effectiveAvatarUrl))
							.setFooter(WebhookEmbed.EmbedFooter(locale["modules.eventLog.userID", message.member?.user?.id], null))
							.setTimestamp(Instant.now())

						sendMessageInEventLogViaWebhook(
							loritta,
							WebhookMessageBuilder()
								.setUsername(message.guild.selfMember.user.name)
								.setAvatarUrl(message.guild.selfMember.user.effectiveAvatarUrl)
								.setContent(" ")
								.addEmbeds(embed.build())
								.build(),
							message.guild,
							eventLogConfig
						)
					}

					if (storedMessage != null) {
						loritta.newSuspendedTransaction {
							storedMessage.encryptAndSetContent(loritta, message.contentRaw)
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
				val textChannel = member.guild.getGuildMessageChannelById(eventLogConfig.eventLogChannelId) ?: return
				val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)

				if (!textChannel.canTalk())
					return
				if (!member.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return
				if (!member.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return

				val embed = WebhookEmbedBuilder()
					.setColor(Color(35, 209, 96).rgb)
					.setDescription("\uD83D\uDC49\uD83C\uDFA4 **${locale["modules.eventLog.joinedVoiceChannel", member.asMention, channelJoined.name]}**")
					.setAuthor(WebhookEmbed.EmbedAuthor("${member.user.name}#${member.user.discriminator}", null, member.user.effectiveAvatarUrl))
					.setFooter(WebhookEmbed.EmbedFooter(locale["modules.eventLog.userID", member.user.id], null))
					.setTimestamp(Instant.now())

				sendMessageInEventLogViaWebhook(
					loritta,
					WebhookMessageBuilder()
						.setUsername(member.guild.selfMember.user.name)
						.setAvatarUrl(member.guild.selfMember.user.effectiveAvatarUrl)
						.setContent(" ")
						.addEmbeds(embed.build())
						.build(),
					channelJoined.guild,
					eventLogConfig
				)
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
				val textChannel = member.guild.getGuildMessageChannelById(eventLogConfig.eventLogChannelId) ?: return
				val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
				if (!textChannel.canTalk())
					return
				if (!member.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return
				if (!member.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return

				val embed = WebhookEmbedBuilder()
					.setColor(Color(35, 209, 96).rgb)
					.setDescription("\uD83D\uDC48\uD83C\uDFA4 **${locale["modules.eventLog.leftVoiceChannel", member.asMention, channelLeft.name]}**")
					.setAuthor(WebhookEmbed.EmbedAuthor("${member.user.name}#${member.user.discriminator}", null, member.user.effectiveAvatarUrl))
					.setFooter(WebhookEmbed.EmbedFooter(locale["modules.eventLog.userID", member.user.id], null))
					.setTimestamp(Instant.now())

				sendMessageInEventLogViaWebhook(
					loritta,
					WebhookMessageBuilder()
						.setUsername(member.guild.selfMember.user.name)
						.setAvatarUrl(member.guild.selfMember.user.effectiveAvatarUrl)
						.setContent(" ")
						.addEmbeds(embed.build())
						.build(),
					channelLeft.guild,
					eventLogConfig
				)
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