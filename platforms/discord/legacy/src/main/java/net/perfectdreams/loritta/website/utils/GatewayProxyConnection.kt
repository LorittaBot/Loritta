package net.perfectdreams.loritta.website.utils

import kotlinx.coroutines.channels.Channel

data class GatewayProxyConnection(
    val channel: Channel<String>
)