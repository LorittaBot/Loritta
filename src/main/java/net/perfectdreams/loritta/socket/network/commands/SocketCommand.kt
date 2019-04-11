package net.perfectdreams.loritta.socket.network.commands

import com.google.gson.JsonObject

abstract class SocketCommand(val op: Int) {
    abstract suspend fun process(payload: JsonObject): JsonObject
}