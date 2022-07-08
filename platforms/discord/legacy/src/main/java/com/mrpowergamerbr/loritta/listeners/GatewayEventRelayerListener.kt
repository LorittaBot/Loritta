package com.mrpowergamerbr.loritta.listeners

import com.mrpowergamerbr.loritta.Loritta
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.events.RawGatewayEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.website.utils.GatewayProxyConnection

class GatewayEventRelayerListener(val m: Loritta) : ListenerAdapter() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun onRawGateway(event: RawGatewayEvent) {
        GlobalScope.launch(m.coroutineDispatcher) {
            val packageAsString = event.`package`.toString()

            val failedChannels = mutableSetOf<GatewayProxyConnection>()
            for (connection in m.connectedChannels) {
                try {
                    connection.channel.send(packageAsString)
                } catch (e: ClosedSendChannelException) {
                    logger.warn { "Gateway Proxy channel is closed! We will remove it from the connected channels list and ignore it then..." }
                    failedChannels.add(connection)
                }
            }

            m.connectedChannels.removeAll(failedChannels)
        }
    }
}