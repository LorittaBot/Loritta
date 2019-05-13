package net.perfectdreams.loritta.socket.network.commands

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.perfectdreams.loritta.socket.network.SocketOpCode
import net.perfectdreams.loritta.utils.extensions.set

class GetGuildByIdCommand : SocketCommand(SocketOpCode.GET_GUILD_BY_ID) {
    override suspend fun process(payload: JsonNode): JsonNode {
        val userId = payload["guildId"].textValue()

        val guild = lorittaShards.getGuildById(userId) ?: return JsonNodeFactory.instance.objectNode()

        val objNode = JsonNodeFactory.instance.objectNode()

        objNode["foundInShard"] = 0
        objNode["guild"] = JsonNodeFactory.instance.objectNode()
                .put("id", guild.id)
                .put("name", guild.name)
                .put("iconUrl", guild.iconUrl).apply {
                    putArray("members")
                    putArray("messageChannels")
                }


        println("objNode: " + objNode)

        return objNode
    }
}