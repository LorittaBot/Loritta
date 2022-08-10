package net.perfectdreams.loritta.cinnamon.discord.webserver.gateway

import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.gateway.KordDiscordEventUtils
import net.perfectdreams.loritta.cinnamon.discord.webserver.utils.config.ReplicaInstanceConfig
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
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
    private val sqlStatements = (replicaInstance.minShard..replicaInstance.maxShard step totalConnections).map {
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
        // And trying to manually add shard = 1 OR shard = 2... does fix the performance issue a bit, but it is still bad
        // loritta_queues=# EXPLAIN ANALYZE SELECT "id", "type", "shard", "payload" FROM discordgatewayevents WHERE shard = 695 OR shard = 703 ORDER BY id LIMIT 10000;
        //                                                                                 QUERY PLAN
        //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // Limit  (cost=13.04..13.05 rows=3 width=903) (actual time=0.544..0.546 rows=0 loops=1)
        //   ->  Sort  (cost=13.04..13.05 rows=3 width=903) (actual time=0.544..0.545 rows=0 loops=1)
        //         Sort Key: discordgatewayevents.id
        //         Sort Method: quicksort  Memory: 25kB
        //         ->  Append  (cost=2.47..13.01 rows=3 width=903) (actual time=0.540..0.541 rows=0 loops=1)
        //               ->  Bitmap Heap Scan on discordgatewayevents_shard_695 discordgatewayevents_1  (cost=2.47..3.60 rows=1 width=1097) (actual time=0.333..0.334 rows=0 loops=1)
        //                     Recheck Cond: ((shard = 695) OR (shard = 703))
        //                     ->  BitmapOr  (cost=2.47..2.47 rows=1 width=0) (actual time=0.030..0.031 rows=0 loops=1)
        //                           ->  Bitmap Index Scan on discordgatewayevents_shard_695_shard_idx  (cost=0.00..1.23 rows=1 width=0) (actual time=0.029..0.029 rows=1044 loops=1)
        //                                 Index Cond: (shard = 695)
        //                           ->  Bitmap Index Scan on discordgatewayevents_shard_695_shard_idx  (cost=0.00..1.23 rows=1 width=0) (actual time=0.001..0.001 rows=0 loops=1)
        //                                 Index Cond: (shard = 703)
        //               ->  Bitmap Heap Scan on discordgatewayevents_shard_703 discordgatewayevents_2  (cost=7.13..9.37 rows=2 width=807) (actual time=0.206..0.207 rows=0 loops=1)
        //                     Recheck Cond: ((shard = 695) OR (shard = 703))
        //                     ->  BitmapOr  (cost=7.13..7.13 rows=2 width=0) (actual time=0.029..0.030 rows=0 loops=1)
        //                           ->  Bitmap Index Scan on discordgatewayevents_shard_703_shard_idx  (cost=0.00..2.46 rows=1 width=0) (actual time=0.006..0.006 rows=0 loops=1)
        //                                 Index Cond: (shard = 695)
        //                           ->  Bitmap Index Scan on discordgatewayevents_shard_703_shard_idx  (cost=0.00..4.67 rows=2 width=0) (actual time=0.023..0.023 rows=655 loops=1)
        //                                 Index Cond: (shard = 703)
        // ...
        // What can we do?
        // We will abuse the fact that we create a different table for each shard, and then query it directly using different statements!
        // loritta_queues=# EXPLAIN ANALYZE SELECT "id", "type", "shard", "payload" FROM discordgatewayevents_shard_384 ORDER BY id LIMIT 10000;
        //                                                                                  QUERY PLAN
        //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // Limit  (cost=0.13..31.06 rows=8 width=1298) (actual time=0.047..0.047 rows=0 loops=1)
        //   ->  Index Scan using discordgatewayevents_shard_384_pkey on discordgatewayevents_shard_384  (cost=0.13..31.06 rows=8 width=1298) (actual time=0.046..0.046 rows=0 loops=1)
        // Planning Time: 0.053 ms
        // Execution Time: 0.056 ms
        // (4 rows)
        // Look, ok, 0.056ms for each shard, let's do some calculations then
        // The full:tm: 40 shards query with WHERE shard = 1 OR ... = 43.639 ms
        // Direct Table access = 0.056
        // 0.056 * 40 = 2.24 ms!!!
        // So let's go... even further beyond!
        val trueTableName = DISCORD_GATEWAY_EVENTS_TABLE + "_shard_${it + connectionId}"
        """DELETE FROM $trueTableName USING (SELECT "id", "type", "shard", "payload" FROM $trueTableName ORDER BY id FOR UPDATE SKIP LOCKED LIMIT $totalEventsPerBatch) q WHERE q.id = $trueTableName.id RETURNING $trueTableName.*;"""
    }

    @OptIn(ExperimentalTime::class)
    override fun run() {
        for (statement in sqlStatements) {
            logger.info { "SQL Statement to be used in the event processor (conn ID: $connectionId): $statement" }
        }

        while (true) {
            try {
                val connection = queueDatabaseDataSource.connection
                var processedOnThisRound = 0
                val duration = measureTime {
                    connection.use {
                        for (sql in sqlStatements) {
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
                                    val proxiedKordGateway = proxiedKordGateways[shardId]
                                        ?: error("Received event for shard ID $shardId, but we don't have a ProxiedKordGateway instance associated with it!")
                                    coroutineScope.launch {
                                        proxiedKordGateway.events.emit(discordEvent)
                                    }
                                } else {
                                    logger.warn { "Unknown Discord event received ($type)! We are going to ignore the event... kthxbye!" }
                                }

                                count++
                                totalEventsProcessed++
                                processedOnThisRound++
                            }
                        }

                        it.commit()
                    }
                }
                lastPollDuration = duration
                totalPollLoopsCount++
                if (processedOnThisRound != totalEventsPerBatch) {
                    // Sleep for 100ms to avoid overloading the system's CPU (database and the app itself)
                    // However, if our query took more than 100ms, we won't sleep!
                    // TODO: Notification System: Make Loritta publish a notification when a new event is posted, instead of polling and sleeping
                    val sleepTime = (100.milliseconds - duration).toLong(DurationUnit.MILLISECONDS)
                    if (sleepTime > 0)
                        Thread.sleep(sleepTime)
                }
            } catch (e: Exception) {
                logger.warn(e) { "Something went wrong while polling pending Discord gateway events!" }
            }
        }
    }
}