package com.mrpowergamerbr.loritta.listeners

import com.mrpowergamerbr.loritta.Loritta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.events.RawGatewayEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class GatewayEventRelayerListener(val m: Loritta) : ListenerAdapter() {
    override fun onRawGateway(event: RawGatewayEvent) {
        GlobalScope.launch(m.coroutineDispatcher) {
            val packageAsString = event.`package`.toString()

            withContext(Dispatchers.IO) {
                m.jedisPool.resource.use {
                    it.rpush(m.redisKey("discord_gateway_events:shard_${event.jda.shardInfo.shardId}"), packageAsString)
                }
            }
        }
    }
}