package net.perfectdreams.loritta.website.utils

import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.Channel

data class GatewayProxyConnection(
    val client: WebSocketServerSession,
    val channel: Channel<String>
) {
    suspend fun close() {
        channel.close()
        client.close(CloseReason(CloseReason.Codes.NORMAL, "Connection closed"))
    }
}