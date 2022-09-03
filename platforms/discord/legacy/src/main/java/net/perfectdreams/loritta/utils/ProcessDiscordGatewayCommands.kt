package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.lettuce.core.LMPopArgs
import io.lettuce.core.RedisClient
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.JDAImpl
import org.postgresql.jdbc.PgConnection
import kotlin.time.Duration.Companion.seconds

class ProcessDiscordGatewayCommands(
    private val loritta: Loritta,
    private val redisClient: RedisClient
) : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val redisConnection = redisClient.connect()
    private val syncCommands = redisConnection.sync()

    private val shardsHandledByThisProcessor = (loritta.lorittaCluster.minShard..loritta.lorittaCluster.maxShard)
    private val keys = shardsHandledByThisProcessor.map {
        loritta.redisKey("discord_gateway_commands:shard_$it")
    }.toTypedArray()

    override fun run() {
        while (true) {
            try {
                val receivedShardCommands = mutableMapOf<Int, MutableList<String>>()

                // Wait until new command is received...
                // This will return all available command of the first queue that has any pending command to be processed
                val eventsBlock = syncCommands.blmpop(
                    // Why the timeout can't be infinite: https://lettuce.io/core/release/reference/#faq.timeout.blpop
                    // The reason it is 60s is because Lettuce's default timeout is also 60s, and if you don't set a timeout here,
                    // the command will still be in Lettuce's command queue, so Lettuce will execute the command and just discard the response.
                    60,
                    LMPopArgs.Builder.left().count(1),
                    *keys
                )

                if (eventsBlock == null) {
                    logger.info { "List pop block call has timed out! This means that there wasn't any new events to be processed. Trying again..." }
                    continue
                }

                receivedShardCommands.getOrPut(getShardIdFromQueue(eventsBlock.key)) { mutableListOf() }
                    .addAll(eventsBlock.value)

                // Events were received, check the length of every list... (We need to check every list because we don't know if any other queue may have events)
                val queueLengths = keys.associateWith {
                    syncCommands.llen(it)
                }

                for ((queue, length) in queueLengths.filterValues { it != 0L }) {
                    // Then do a lmpop to get all the pending events
                    val r = syncCommands.lmpop(LMPopArgs.Builder.left().count(length), queue)

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
            } catch (e: Exception) {
                logger.warn(e) { "Something went wrong while polling pending Discord gateway events!" }
            }
        }
    }

    private fun getShardIdFromQueue(queue: String) = queue.substringAfterLast("_").toInt()
}