package com.mrpowergamerbr.loritta.listeners

import com.mrpowergamerbr.loritta.Loritta
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.api.coroutines
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import mu.KotlinLogging
import net.dv8tion.jda.api.events.RawGatewayEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.LorittaNotification
import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.NewDiscordGatewayEventNotification
import org.postgresql.util.PGobject
import java.sql.Timestamp
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*

@OptIn(ExperimentalLettuceCoroutinesApi::class)
class GatewayEventRelayerListener(val m: Loritta) : ListenerAdapter() {
    private val redisConnection = m.redisClient.connect()
    private val syncCommands = redisConnection.coroutines()

    override fun onRawGateway(event: RawGatewayEvent) {
        GlobalScope.launch(m.coroutineDispatcher) {
            val packageAsString = event.`package`.toString()

            syncCommands.rpush(m.redisKey("discord_gateway_events:shard_${event.jda.shardInfo.shardId}"), packageAsString)
        }
    }
}