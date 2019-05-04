package net.perfectdreams.loritta.socket.network.commands

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.perfectdreams.loritta.platform.discord.entities.DiscordUser
import net.perfectdreams.loritta.socket.network.SocketOpCode
import net.perfectdreams.loritta.utils.extensions.set

class GetUserByIdCommand : SocketCommand(SocketOpCode.GET_USER_BY_ID) {
    override suspend fun process(payload: JsonNode): JsonNode {
        val userId = payload["userId"].textValue()

        val user = lorittaShards.getUserById(userId) ?: return JsonNodeFactory.instance.objectNode()
        val discordUser = DiscordUser(user)

        val objNode = JsonNodeFactory.instance.objectNode()

        objNode["foundInShard"] = 0
        objNode["user"] = discordUser

        println("objNode: " + objNode)

        return objNode
    }
}