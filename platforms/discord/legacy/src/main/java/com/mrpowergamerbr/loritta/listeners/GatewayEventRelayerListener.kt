package com.mrpowergamerbr.loritta.listeners

import com.mrpowergamerbr.loritta.Loritta
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

class GatewayEventRelayerListener(val m: Loritta) : ListenerAdapter() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val permits = Semaphore(128)
    var hasEventsBeenDroppedYet = false

    override fun onRawGateway(event: RawGatewayEvent) {
        // We will limit the pending gateway events to avoid having too many pending events to be processed
        if (m.config.gatewayProxy.maxPendingEventsThreshold > m.pendingGatewayEventsCount) {
            hasEventsBeenDroppedYet = false
            GlobalScope.launch(m.coroutineDispatcher) {
                val packageAsString = event.`package`.toString()

                // Used to avoid having a lot of threads being created on the "dispatcher" just to be blocked waiting for a connection, causing thread starvation and an OOM kill
                permits.withPermit {
                    m.queueConnection.connection.use {
                        val statement = it.prepareStatement("INSERT INTO discordgatewayevents (type, received_at, shard, payload) VALUES (?, ?, ?, ?);")
                        statement.setString(1, event.type)
                        statement.setObject(2, OffsetDateTime.now())
                        statement.setInt(3, event.jda.shardInfo.shardId)
                        val pgObject = PGobject()
                        pgObject.type = "jsonb"
                        pgObject.value = packageAsString
                        statement.setObject(4, pgObject)
                        statement.executeUpdate()

                        // Submit new gateway event notification
                        val notificationStatement = it.prepareStatement("SELECT pg_notify('gateway_events_shard_${event.jda.shardInfo.shardId}', NULL)")
                        notificationStatement.execute()

                        it.commit()
                    }
                }
            }
        } else if (!hasEventsBeenDroppedYet) {
            logger.warn { "Dropping Gateway Events because there is ${m.pendingGatewayEventsCount} pending events, more than the ${m.config.gatewayProxy.maxPendingEventsThreshold} threshold!" }

            hasEventsBeenDroppedYet = true
        }
    }
}