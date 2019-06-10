package net.perfectdreams.spicymorenitta.ws

import net.perfectdreams.spicymorenitta.utils.Logging
import org.w3c.dom.WebSocket
import kotlin.js.Json

abstract class WebSocketCommand(val name: String) : Logging {
    abstract fun process(socket: WebSocket, json: Json)
}