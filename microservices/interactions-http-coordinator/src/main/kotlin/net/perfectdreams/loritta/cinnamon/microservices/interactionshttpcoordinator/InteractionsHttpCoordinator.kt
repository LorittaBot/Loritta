package net.perfectdreams.loritta.cinnamon.microservices.interactionshttpcoordinator

import dev.kord.common.entity.DiscordInteraction
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.interactionshttpcoordinator.config.InteractionsHttpCoordinatorConfig
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/**
 * Coordinates HTTP interactions to their specific stateful instance, based on the interactions' guild ID.
 *
 * If the guild ID is not set, the interaction will be forwarded to the first registered instance.
 */
class InteractionsHttpCoordinator(private val config: InteractionsHttpCoordinatorConfig) {
    companion object {
        private val logger = KotlinLogging.logger {}

        private val JsonIgnoreUnknownKeys = Json {
            ignoreUnknownKeys = true
        }
    }

    private val http = HttpClient(CIO)

    @OptIn(ExperimentalTime::class)
    fun start() {
        val server = embeddedServer(Netty, 8080) {
            routing {
                post("/") {
                    try {
                        val body = call.receiveText()
                        val event = JsonIgnoreUnknownKeys.decodeFromString<DiscordInteraction>(body)

                        // Let's coordinate!
                        val guildId = event.guildId.value

                        val instance = if (guildId == null) {
                            // Guild ID not set, forward to first registered instance!
                            config.instances.first()
                        } else {
                            val shardId = getShardIdFromGuildId(guildId.value.toLong())

                            config.instances.firstOrNull { shardId in it.minShard..it.maxShard } ?: error("I couldn't find a matching instance for $it! Was Loritta resharded and you forgot to reconfigure the shard ranges on the configuration?")
                        }

                        logger.info { "Forwarding ${event.id} request to ${instance.url}! Guild ID: $guildId" }

                        // Forward the request as is
                        val duration = measureTime {
                            http.post(instance.url) {
                                headers {
                                    appendAll(call.request.headers)
                                }

                                setBody(body)
                            }
                        }

                        logger.info { "Request ${event.id} was successfully forwarded to ${instance}! Guild ID: $guildId - Took $duration" }
                    } catch (e: Exception) {
                        logger.warn(e) { "Something went wrong while trying to forward the request!" }
                    }
                }
            }
        }
        server.start(true)
    }

    private fun getShardIdFromGuildId(id: Long): Int {
        val maxShard = config.totalShards
        return (id shr 22).rem(maxShard).toInt()
    }
}