package com.mrpowergamerbr.loritta.listeners

import com.mrpowergamerbr.loritta.Loritta
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.events.RawGatewayEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class GatewayEventRelayerListener(val m: Loritta) : ListenerAdapter() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun onRawGateway(event: RawGatewayEvent) {
        GlobalScope.launch(m.coroutineDispatcher) {
            val packageAsString = event.`package`.toString()

            for (connection in m.connectedChannels) {
                try {
                    connection.channel.send(packageAsString)
                } catch (e: ClosedSendChannelException) {
                    // Technically this could only happen if the .send is waiting for the WS to receive the event BEFORE the connection is closed
                    // The clean up stuff should be triggered on the WebSocket route itself
                    logger.warn(e) { "Gateway Proxy channel is closed! We will ignore it then..." }
                }
            }
        }
    }
}