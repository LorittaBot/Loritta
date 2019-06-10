package net.perfectdreams.loritta.socket.network.commands

import com.fasterxml.jackson.databind.JsonNode
import net.perfectdreams.loritta.socket.SocketWrapper

abstract class SocketCommand(val op: Int) {
    abstract suspend fun process(socketWrapper: SocketWrapper, payload: JsonNode): JsonNode
}