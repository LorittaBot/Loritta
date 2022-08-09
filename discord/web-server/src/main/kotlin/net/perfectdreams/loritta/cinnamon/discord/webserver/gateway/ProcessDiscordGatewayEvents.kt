package net.perfectdreams.loritta.cinnamon.discord.webserver.gateway

import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.gateway.KordDiscordEventUtils
import net.perfectdreams.loritta.cinnamon.discord.webserver.utils.config.ReplicaInstanceConfig

/**
 * Processes Discord Gateway Events stored on a PostgreSQL table
 *
 * Yeah, that's a Devious SQL™ moment https://www.crunchydata.com/blog/message-queuing-using-native-postgresql
 */
class ProcessDiscordGatewayEvents(
    private val totalEventsPerBatch: Int,
    val totalConnections: Int,
    val connectionId: Int,
    replicaInstance: ReplicaInstanceConfig,
    private val queueDatabaseDataSource: HikariDataSource,
    // Shard ID -> ProxiedKordGateway
    private val proxiedKordGateways: Map<Int, ProxiedKordGateway>
) : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
        const val DISCORD_GATEWAY_EVENTS_TABLE = "discordgatewayevents"
    }

    var totalEventsProcessed = 0L
    var totalPollLoopsCount = 0L

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val sql = """DELETE FROM $DISCORD_GATEWAY_EVENTS_TABLE USING (SELECT "id", "type", "shard", "payload" FROM $DISCORD_GATEWAY_EVENTS_TABLE WHERE shard BETWEEN ${replicaInstance.minShard} AND ${replicaInstance.maxShard} AND shard % $totalConnections = $connectionId ORDER BY id FOR UPDATE SKIP LOCKED LIMIT $totalEventsPerBatch) q WHERE q.id = $DISCORD_GATEWAY_EVENTS_TABLE.id RETURNING $DISCORD_GATEWAY_EVENTS_TABLE.*;"""

    override fun run() {
        while (true) {
            try {
                val connection = queueDatabaseDataSource.connection
                connection.use {
                    val selectStatement = it.prepareStatement(sql)
                    val rs = selectStatement.executeQuery()

                    var count = 0
                    val processedRows = mutableListOf<Long>()

                    while (rs.next()) {
                        val id = rs.getLong("id")
                        val type = rs.getString("type")
                        val shardId = rs.getInt("shard")
                        val gatewayPayload = rs.getString("payload")

                        val discordEvent = KordDiscordEventUtils.parseEventFromString(gatewayPayload)

                        if (discordEvent != null) {
                            // Emit the event to our proxied instances
                            val proxiedKordGateway = proxiedKordGateways[shardId] ?: error("Received event for shard ID $shardId, but we don't have a ProxiedKordGateway instance associated with it!")
                            coroutineScope.launch {
                                proxiedKordGateway.events.emit(discordEvent)
                            }
                        } else {
                            logger.warn { "Unknown Discord event received ($type)! We are going to ignore the event... kthxbye!" }
                        }

                        count++
                        totalEventsProcessed++

                        processedRows.add(id)
                    }

                    it.commit()
                }
                totalPollLoopsCount++
            } catch (e: Exception) {
                logger.warn(e) { "Something went wrong while polling pending Discord gateway events!" }
            }
        }
    }
}