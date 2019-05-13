package net.perfectdreams.loritta.socket.network.commands

import com.fasterxml.jackson.databind.JsonNode

abstract class SocketCommand(val op: Int) {
    abstract suspend fun process(payload: JsonNode): JsonNode
}