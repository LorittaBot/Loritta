package net.perfectdreams.loritta.socket.network.commands

import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonObject
import net.perfectdreams.loritta.socket.network.SocketOpCode

class UpdateStatusCommand : SocketCommand(SocketOpCode.UPDATE_STATUS_COUNT) {
    override suspend fun process(payload: JsonObject): JsonObject {
        val guildCount = payload["guildCount"].int
        val memberCount = payload["memberCount"].int

        println("guildCount is ${guildCount}")
        println("memberCount is ${memberCount}")

        return jsonObject()
    }
}