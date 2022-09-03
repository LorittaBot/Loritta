package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import mu.KotlinLogging
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.JDAImpl
import redis.clients.jedis.JedisPool
import redis.clients.jedis.args.ListDirection
import redis.clients.jedis.exceptions.JedisException

class ProcessDiscordGatewayCommands(
    private val loritta: Loritta,
    private val jedisPool: JedisPool
) : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val shardsHandledByThisProcessor = (loritta.lorittaCluster.minShard..loritta.lorittaCluster.maxShard)
    private val keys = shardsHandledByThisProcessor.map {
        loritta.redisKey("discord_gateway_commands:shard_$it")
    }.toTypedArray()

    override fun run() {
        while (true) {
            try {
                jedisPool.resource.use {
                    while (true) {
                        try {
                            val receivedShardCommands = mutableMapOf<Int, MutableList<String>>()

                            // Wait until new command is received...
                            // This will return all available command of the first queue that has any pending command to be processed
                            val eventsBlock = it.blmpop(
                                60,
                                ListDirection.LEFT,
                                1,
                                *keys
                            )

                            if (eventsBlock == null) {
                                logger.info { "List pop block call has timed out! This means that there wasn't any new events to be processed. Trying again..." }
                                continue
                            }

                            receivedShardCommands.getOrPut(getShardIdFromQueue(eventsBlock.key)) { mutableListOf() }
                                .addAll(eventsBlock.value)

                            // Events were received, check the length of every list... (We need to check every list because we don't know if any other queue may have events)
                            val queueLengths = keys.associateWith { queue ->
                                it.llen(queue)
                            }

                            for ((queue, length) in queueLengths.filterValues { it != 0L }) {
                                // Then do a lmpop to get all the pending events
                                val r = it.lmpop(ListDirection.LEFT, length.toInt(), queue)

                                if (r == null) {
                                    logger.warn { "lmpop on $queue is null! This should never happen!" }
                                    continue
                                }

                                receivedShardCommands.getOrPut(getShardIdFromQueue(r.key)) { mutableListOf() }
                                    .addAll(r.value)
                            }

                            for ((shardId, events) in receivedShardCommands) {
                                for (event in events) {
                                    // "babe wake up new gateway command on shard 5 just dropped"
                                    val jdaShard = lorittaShards.shardManager.getShardById(shardId) as JDAImpl?

                                    if (jdaShard != null) {
                                        logger.info { "Sending gateway command $event to $shardId" }
                                        jdaShard.client.send(DataObject.fromJson(event))
                                    } else {
                                        logger.warn { "Received a gateway event notification for a shard that we don't handle (shard ID: $shardId)! This should never happen!" }
                                    }
                                }
                            }
                        } catch (e: JedisException) {
                            logger.warn(e) { "Something went wrong with the Jedis' connection!" }
                            throw e
                        } catch (e: Exception) {
                            logger.warn(e) { "Something went wrong while polling pending Discord gateway commands!" }
                        }
                    }
                }
            } catch (e: Exception) {
                logger.warn(e) { "Something went wrong while acquiring Redis connection!" }
            }
            logger.warn { "Left the connection loop, reconnecting..." }
        }
    }

    private fun getShardIdFromQueue(queue: String) = queue.substringAfterLast("_").toInt()
}