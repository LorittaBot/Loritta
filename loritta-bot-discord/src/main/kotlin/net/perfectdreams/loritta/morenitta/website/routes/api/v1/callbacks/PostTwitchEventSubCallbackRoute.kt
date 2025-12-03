package net.perfectdreams.loritta.morenitta.website.routes.api.v1.callbacks

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.cinnamon.pudding.tables.TwitchEventSubEvents
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.rpc.LorittaRPC
import net.perfectdreams.loritta.morenitta.rpc.execute
import net.perfectdreams.loritta.morenitta.rpc.payloads.TwitchStreamOnlineEventResponse
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.switchtwitch.data.TwitchStream
import net.perfectdreams.switchtwitch.data.VerificationRequest
import net.perfectdreams.switchtwitch.data.events.TwitchEventRequest
import net.perfectdreams.switchtwitch.data.events.TwitchStreamOnlineEventRequest
import org.jetbrains.exposed.sql.insert
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.time.Duration.Companion.seconds

class PostTwitchEventSubCallbackRoute(val loritta: LorittaBot) : BaseRoute("/api/v1/callbacks/twitch-eventsub") {
	companion object {
		private val logger by HarmonyLoggerFactory.logger {}
		private const val MESSAGE_SIGNATURE_HEADER = "Twitch-Eventsub-Message-Signature"
		private const val MESSAGE_ID_HEADER = "Twitch-Eventsub-Message-Id"
		private const val MESSAGE_TIMESTAMP_HEADER = "Twitch-Eventsub-Message-Timestamp"
	}

	override suspend fun onRequest(call: ApplicationCall) {
		val response = withContext(Dispatchers.IO) {
			call.receiveStream().bufferedReader(charset = Charsets.UTF_8).readText()
		}

		logger.info { "Received Request from Twitch! $response" }

		val result = verifyRequest(call, response, loritta.config.loritta.twitch.webhookSecret)
		if (!result) {
			logger.warn { "Failed to verify Twitch request!" }
			call.respondText("", ContentType.Application.Json, HttpStatusCode.Unauthorized)
			return
		}

		val jsonResponse = Json.parseToJsonElement(response)
			.jsonObject

		val challenge = jsonResponse.jsonObject["challenge"]

		if (challenge != null) {
			val challengeResponse = Json.decodeFromJsonElement<VerificationRequest>(jsonResponse)

			// If the challenge field is not null, then it means Twitch wants to verify a webhook!
			logger.info { "Verifying Twitch webhook EventSub request..." }
			call.respondText(challengeResponse.challenge)
		} else {
			// If not, then it is a EventSub event!
			logger.info { "Received a EventSub Request from Twitch! Inserting to database..." }
			loritta.transaction {
				TwitchEventSubEvents.insert {
					it[event] = jsonResponse.toString()
				}
			}

			when (val eventRequest = TwitchEventRequest.from(jsonResponse)) {
				is TwitchStreamOnlineEventRequest -> {
					GlobalScope.launch {
						val event = eventRequest.event
						val broadcasterUserLogin = event.broadcasterUserLogin
						val broadcasterUserIdAsLong = event.broadcasterUserId.toLong()

						// We only care about sending the data to other Loritta instances here
						// But before we do that... Let's load the stream info!
						var stream: TwitchStream? = null

						// We are going to try up to 1 minute
						// 12 * 5 = 60 seconds

						for (index in 0 until 12) {
							logger.info { "Querying ${broadcasterUserIdAsLong}'s Twitch stream info... Tries: $index" }
							val streamInfo = loritta.switchTwitch.getStreamsByUserId(broadcasterUserIdAsLong)
								.firstOrNull()

							if (streamInfo != null) {
								// Found it!
								stream = streamInfo
								break
							}

							logger.warn { "Couldn't find ${broadcasterUserIdAsLong}'s Twitch stream info! Trying again in 5s... Tries: $index" }

							delay(5.seconds)
						}

						logger.info { "$broadcasterUserIdAsLong's Twitch stream info is $stream" }

						val jobs = loritta.config.loritta.clusters.instances.map { cluster ->
							cluster to GlobalScope.async {
								withTimeout(25_000) {
                                    LorittaRPC.TwitchStreamOnlineEvent.execute(
                                        loritta,
                                        cluster,
                                        net.perfectdreams.loritta.morenitta.rpc.payloads.TwitchStreamOnlineEventRequest(
                                            broadcasterUserIdAsLong,
                                            broadcasterUserLogin,
                                            stream?.title,
                                            stream?.gameName
                                        )
                                    )
								}
							}
						}

						var totalNotifiedCount = 0
						for (job in jobs) {
							try {
								val relayResult = job.second.await()
                                if (relayResult is TwitchStreamOnlineEventResponse.Success) {
                                    logger.info { "Twitch Relay of $broadcasterUserLogin ($broadcasterUserIdAsLong) to Cluster ${job.first.id} (${job.first.name}) was successfully processed! Notified Guilds: ${relayResult.notifiedGuilds.size}" }
                                    totalNotifiedCount += relayResult.notifiedGuilds.size
                                } else {
                                    error("Relay result is not Success! Result: $relayResult")
                                }
							} catch (e: Exception) {
								logger.warn(e) { "Twitch Relay of $broadcasterUserLogin ($broadcasterUserIdAsLong) to Cluster ${job.first.id} (${job.first.name}) failed!" }
							}
						}
						logger.info { "Twitch Relay of $broadcasterUserLogin ($broadcasterUserIdAsLong) completed! Notification Count: $totalNotifiedCount" }
					}
				}
				else -> error("I don't know how to handle a ${eventRequest}!")
			}

			call.respondText("", ContentType.Application.Json, HttpStatusCode.OK) // yay!
		}
	}

	fun verifyRequest(
		call: ApplicationCall,
		response: String,
		webhookSecret: String
	) = verifyRequest(
		call.request.header(MESSAGE_SIGNATURE_HEADER) ?: error("Missing Signature Header!"),
		call.request.header(MESSAGE_ID_HEADER) ?: error("Missing Message ID Header!"),
		call.request.header(MESSAGE_TIMESTAMP_HEADER) ?: error("Missing Message Timestamp Header!"),
		response,
		webhookSecret
	)

	fun verifyRequest(
		messageSignature: String,
		messageId: String,
		messageTimestamp: String,
		response: String,
		webhookSecret: String
	): Boolean {
		// Signature Verification: https://dev.twitch.tv/docs/eventsub#verify-a-signature
		val hmacMessage = messageId + messageTimestamp + response
		val signingKey = SecretKeySpec(webhookSecret.toByteArray(Charsets.UTF_8), "HmacSHA256")
		val mac = Mac.getInstance("HmacSHA256")
		mac.init(signingKey)
		val doneFinal = mac.doFinal(hmacMessage.toByteArray(Charsets.UTF_8))
		val expectedSignatureHeader = "sha256=" + doneFinal.bytesToHex()

		return messageSignature == expectedSignatureHeader
	}

	/**
	 * Converts a ByteArray to a hexadecimal string
	 *
	 * @return the byte array in hexadecimal format
	 */
	fun ByteArray.bytesToHex(): String {
		val hexString = StringBuffer()
		for (i in this.indices) {
			val hex = Integer.toHexString(0xff and this[i].toInt())
			if (hex.length == 1) {
				hexString.append('0')
			}
			hexString.append(hex)
		}
		return hexString.toString()
	}
}