package com.mrpowergamerbr.loritta.listeners

import com.mrpowergamerbr.loritta.Loritta
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import net.dv8tion.jda.api.events.RawGatewayEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.tables.DiscordGatewayEvents
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class GatewayEventRelayerListener(val m: Loritta) : ListenerAdapter() {
    private val semaphore = Semaphore(4)

    override fun onRawGateway(event: RawGatewayEvent) {
        val receivedAt = Instant.now()

        GlobalScope.launch(m.coroutineDispatcher) {
            // Just to avoid a LOT of threads being created at the same time, causing a "java.lang.OutOfMemoryError: unable to create native thread: possibly out of memory or process/resource limits reached"
            semaphore.withPermit {
                transaction(m.queueDatabase) {
                    DiscordGatewayEvents.insert {
                        it[DiscordGatewayEvents.type] = event.type
                        it[DiscordGatewayEvents.receivedAt] = receivedAt
                        it[DiscordGatewayEvents.shard] = event.jda.shardInfo.shardId
                        it[DiscordGatewayEvents.payload] = event.`package`.toString()
                    }
                }
            }
        }
    }
}