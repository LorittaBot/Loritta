package net.perfectdreams.loritta.cinnamon.discord.webserver.gateway

import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.gateway.KordDiscordEventUtils
import net.perfectdreams.loritta.cinnamon.discord.webserver.utils.config.ReplicaInstanceConfig
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

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
    var lastPollDuration: Duration? = null

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val sql = buildString {
        append("""DELETE FROM $DISCORD_GATEWAY_EVENTS_TABLE USING (SELECT "id", "type", "shard", "payload" FROM $DISCORD_GATEWAY_EVENTS_TABLE WHERE """)
        // While using "shard BETWEEN ${replicaInstance.minShard} AND ${replicaInstance.maxShard} AND" and "shard % 8 = 0" seems obvious, using those seems to cause PostgreSQL to query
        // EVERY SINGLE TABLE!!
        // Limit  (cost=2.71..9.64 rows=1 width=103) (actual time=0.112..0.113 rows=0 loops=1)
        //   ->  LockRows  (cost=2.71..425.57 rows=61 width=103) (actual time=0.112..0.113 rows=0 loops=1)
        //         ->  Merge Append  (cost=2.71..424.96 rows=61 width=103) (actual time=0.111..0.113 rows=0 loops=1)
        //               Sort Key: discordgatewayevents.id
        //               ->  Index Scan using discordgatewayevents_shard_0_pkey on discordgatewayevents_shard_0 discordgatewayevents_1  (cost=0.14..23.36 rows=1 width=1115) (actual time=0.028..0.028 rows=0 loops=1)
        //                     Filter: ((shard % 8) = 0)
        //               ->  Index Scan using discordgatewayevents_shard_1_pkey on discordgatewayevents_shard_1 discordgatewayevents_2  (cost=0.15..26.65 rows=4 width=86) (actual time=0.010..0.010 rows=0 loops=1)
        //                     Filter: ((shard % 8) = 0)
        //               ->  Index Scan using discordgatewayevents_shard_2_pkey on discordgatewayevents_shard_2 discordgatewayevents_3  (cost=0.15..26.65 rows=4 width=86) (actual time=0.005..0.005 rows=0 loops=1)
        //                     Filter: ((shard % 8) = 0)
        //               ->  Index Scan using discordgatewayevents_shard_3_pkey on discordgatewayevents_shard_3 discordgatewayevents_4  (cost=0.15..26.65 rows=4 width=86) (actual time=0.009..0.009 rows=0 loops=1)
        //                     Filter: ((shard % 8) = 0)
        //               ->  Index Scan using discordgatewayevents_shard_4_pkey on discordgatewayevents_shard_4 discordgatewayevents_5  (cost=0.15..26.65 rows=4 width=86) (actual time=0.004..0.004 rows=0 loops=1)
        //                     Filter: ((shard % 8) = 0)
        //               ->  Index Scan using discordgatewayevents_shard_5_pkey on discordgatewayevents_shard_5 discordgatewayevents_6  (cost=0.15..26.65 rows=4 width=86) (actual time=0.004..0.004 rows=0 loops=1)
        //                     Filter: ((shard % 8) = 0)
        //               ->  Index Scan using discordgatewayevents_shard_6_pkey on discordgatewayevents_shard_6 discordgatewayevents_7  (cost=0.15..26.65 rows=4 width=86) (actual time=0.005..0.005 rows=0 loops=1)
        //                     Filter: ((shard % 8) = 0)
        // ...
        // Thankfully we already know our targets, so we will build our query manually
        val minShard = replicaInstance.minShard
        val maxShard = replicaInstance.maxShard
        val shards = minShard..maxShard step totalConnections
        var isFirst = true
        for (shard in shards) {
            if (!isFirst)
                append(" OR ")
            append("shard = ${shard + connectionId}")
            isFirst = false
        }
        append(""" ORDER BY id FOR UPDATE SKIP LOCKED LIMIT $totalEventsPerBatch) q WHERE q.id = $DISCORD_GATEWAY_EVENTS_TABLE.id RETURNING $DISCORD_GATEWAY_EVENTS_TABLE.*;""")
    }

    @OptIn(ExperimentalTime::class)
    override fun run() {
        println(sql)
        while (true) {
            try {
                val connection = queueDatabaseDataSource.connection
                lastPollDuration = measureTime {
                    connection.use {
                        val selectStatement = it.prepareStatement(sql)
                        val rs = selectStatement.executeQuery()

                        var count = 0

                        while (rs.next()) {
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
                        }
                    }
                }
                totalPollLoopsCount++
            } catch (e: Exception) {
                logger.warn(e) { "Something went wrong while polling pending Discord gateway events!" }
            }
        }
    }
}