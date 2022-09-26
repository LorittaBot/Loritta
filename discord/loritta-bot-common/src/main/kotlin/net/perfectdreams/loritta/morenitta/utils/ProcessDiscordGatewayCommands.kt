package net.perfectdreams.loritta.morenitta.utils

import net.perfectdreams.loritta.morenitta.Loritta
import mu.KotlinLogging
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.JDAImpl
import redis.clients.jedis.JedisPool
import redis.clients.jedis.Response
import redis.clients.jedis.args.ListDirection
import redis.clients.jedis.exceptions.JedisException
import redis.clients.jedis.util.KeyValue

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

                            // Events were received, try popping every list... (We need to check every list because we don't know if any other queue may have events)
                            // We will use pipelining to reduce round trips
                            val lmpopResponses = mutableMapOf<String, Response<KeyValue<String, List<String>>>>()
                            val lmpopPipelines = it.pipelined()

                            for (queue in keys) {
                                // Do a lmpop to get all the pending events
                                lmpopResponses[queue] = lmpopPipelines.lmpop(ListDirection.LEFT, 1_000, queue)
                            }

                            lmpopPipelines.sync()

                            for ((queue, response) in lmpopResponses) {
                                val r = response.get() ?: continue // If null, then it means there wasn't anything to pop

                                receivedShardCommands.getOrPut(getShardIdFromQueue(queue)) { mutableListOf() }
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