package net.perfectdreams.loritta.morenitta.listeners

import net.perfectdreams.loritta.morenitta.Loritta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import mu.KotlinLogging
import net.dv8tion.jda.api.events.RawGatewayEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

class GatewayEventRelayerListener(val m: Loritta) : ListenerAdapter() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val backingOffSince = ConcurrentHashMap<Int, Instant>()

    override fun onRawGateway(event: RawGatewayEvent) {
        val backingOffTime = backingOffSince[event.jda.shardInfo.shardId]

        if (backingOffTime != null && 5.seconds > Clock.System.now() - backingOffTime)
            return

        GlobalScope.launch(m.coroutineDispatcher) {
            val packageAsString = event.`package`.toString()

            withContext(Dispatchers.IO) {
                m.jedisPool.resource.use {
                    val elementsInTheList = it.rpush(m.redisKey("discord_gateway_events:shard_${event.jda.shardInfo.shardId}"), packageAsString)

                    if (elementsInTheList > 5_000) {
                        logger.warn { "Too many elements on ${event.jda.shardInfo.shardId}'s queue ($elementsInTheList events)! We will back off for now..." }
                        backingOffSince[event.jda.shardInfo.shardId] = Clock.System.now()
                    }
                }
            }
        }
    }
}