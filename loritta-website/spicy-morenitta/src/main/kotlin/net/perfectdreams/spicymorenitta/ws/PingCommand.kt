package net.perfectdreams.spicymorenitta.ws

import org.w3c.dom.WebSocket
import kotlin.js.Json

class PingCommand : WebSocketCommand("ping") {
    override fun process(socket: WebSocket, json: Json) {
        info("Ping request received successfully!")
    }
}