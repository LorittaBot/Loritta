package net.perfectdreams.loritta.socket.network.commands

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.perfectdreams.loritta.socket.network.SocketOpCode

class GetUserByIdCommand : SocketCommand(SocketOpCode.GET_USER_BY_ID) {
    override suspend fun process(payload: JsonObject): JsonObject {
        val userId = payload["userId"].long

        val user = lorittaShards.getUserById(userId) ?: return jsonObject()

        return jsonObject(
                "foundInShard" to 0,
                "user" to jsonObject(
                        "id" to user.id,
                        "name" to user.name,
                        "avatarUrl" to user.avatarUrl,
                        "effectiveAvatarUrl" to user.effectiveAvatarUrl
                )
        )
    }
}