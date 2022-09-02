package net.perfectdreams.loritta.cinnamon.discord.webserver.gateway

import io.lettuce.core.LMPopArgs
import io.lettuce.core.api.StatefulRedisConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.gateway.KordDiscordEventUtils
import net.perfectdreams.loritta.cinnamon.discord.webserver.LorittaCinnamonWebServer
import net.perfectdreams.loritta.cinnamon.discord.webserver.utils.config.ReplicaInstanceConfig
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

/**
 * Processes Discord Gateway Events stored on Redis
 */
class ProcessDiscordGatewayEvents(
    private val loritta: LorittaCinnamonWebServer,
    private val totalEventsPerBatch: Long,
    replicaInstance: ReplicaInstanceConfig,
    private val redisConnection: StatefulRedisConnection<String, String>,
    // Shard ID -> ProxiedKordGateway
    private val proxiedKordGateways: Map<Int, ProxiedKordGateway>
) {
    companion object {
        private val logger = KotlinLogging.logger {}
        const val DISCORD_GATEWAY_EVENTS_QUEUE_PREFIX = "discord_gateway_events"
    }

    private val syncCommands = redisConnection.sync()

    var totalEventsProcessed = 0L
    var totalPollLoopsCount = 0L
    var lastPollDuration: Duration? = null
    var lastBlockDuration: Duration? = null
    var isBlockedForNotifications = false

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val shardsHandledByThisProcessor = (replicaInstance.minShard..replicaInstance.maxShard)
    private val keys = shardsHandledByThisProcessor.map {
        loritta.redisKey("${DISCORD_GATEWAY_EVENTS_QUEUE_PREFIX}_shard_$it")
    }.toTypedArray()

    @OptIn(ExperimentalTime::class)
    suspend fun run() {
        while (true) {
            try {
                val receivedShardEvents = mutableMapOf<Int, MutableList<String>>()

                // Wait until new event is received...
                // This will return all availables event of the first queue that has any pending event to be processed
                isBlockedForNotifications = true
                val (eventsBlock, time) = measureTimedValue {
                    syncCommands.blmpop(
                        0,
                        LMPopArgs.Builder.left().count(1),
                        *keys
                    )
                }
                isBlockedForNotifications = false
                this.lastBlockDuration = time

                this.lastPollDuration = measureTime {
                    receivedShardEvents.getOrPut(getShardIdFromQueue(eventsBlock.key)) { mutableListOf() }
                        .addAll(eventsBlock.value)

                    // Events were received, check the length of every list... (We need to check every list because we don't know if any other queue may have events)
                    val queueLengths = keys.associateWith {
                        syncCommands.llen(it).coerceAtMost(totalEventsPerBatch)
                    }

                    for ((queue, length) in queueLengths.filterValues { it != 0L }) {
                        // Then do a lmpop to get all the pending events
                        val r = syncCommands.lmpop(LMPopArgs.Builder.left().count(length), queue)

                        receivedShardEvents.getOrPut(getShardIdFromQueue(r.key)) { mutableListOf() }
                            .addAll(r.value)
                    }

                    for ((shardId, events) in receivedShardEvents) {
                        for (event in events) {
                            // "babe wake up new gateway event on shard 5 just dropped"
                            val discordEvent = KordDiscordEventUtils.parseEventFromString(event)

                            if (discordEvent != null) {
                                // Emit the event to our proxied instances
                                val proxiedKordGateway = proxiedKordGateways[shardId]
                                    ?: error("Received event for shard ID $shardId, but we don't have a ProxiedKordGateway instance associated with it!")

                                coroutineScope.launch {
                                    proxiedKordGateway.events.emit(discordEvent)
                                }
                            } else {
                                logger.warn { "Unknown Discord event received! We are going to ignore the event... kthxbye!" }
                            }

                            totalEventsProcessed++
                        }
                    }
                }
            } catch (e: Exception) {
                logger.warn(e) { "Something went wrong while polling pending Discord gateway events!" }
            }
        }
    }

    private fun getShardIdFromQueue(queue: String) = queue.substringAfterLast("_").toInt()
}