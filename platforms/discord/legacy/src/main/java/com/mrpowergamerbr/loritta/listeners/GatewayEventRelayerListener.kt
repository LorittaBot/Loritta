package com.mrpowergamerbr.loritta.listeners

import com.mrpowergamerbr.loritta.Loritta
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import mu.KotlinLogging
import net.dv8tion.jda.api.events.RawGatewayEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.tables.DiscordGatewayEvents
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class GatewayEventRelayerListener(val m: Loritta) : ListenerAdapter() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val semaphore = Semaphore(4)

    override fun onRawGateway(event: RawGatewayEvent) {
        val receivedAt = Instant.now()

        GlobalScope.launch(m.coroutineDispatcher) {
            // Just to avoid a LOT of threads being created at the same time, causing a "java.lang.OutOfMemoryError: unable to create native thread: possibly out of memory or process/resource limits reached"
            semaphore.withPermit {
                val packageAsString = event.`package`.toString()

                for (channel in m.connectedChannels) {
                    logger.info { "Sending event to $channel" }
                    channel.send(packageAsString)
                }
            }
        }
    }
}