package net.perfectdreams.loritta.socket.network.commands

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import net.perfectdreams.loritta.socket.LorittaController
import net.perfectdreams.loritta.socket.SocketWrapper
import net.perfectdreams.loritta.socket.network.SocketOpCode

class IdentifyCommand(val controller: LorittaController) : SocketCommand(SocketOpCode.Discord.IDENTIFY) {
    override suspend fun process(socketWrapper: SocketWrapper, payload: JsonNode): JsonNode {
        val lorittaShardId = payload["lorittaShardId"].intValue()
        val lorittaShardName = payload["lorittaShardName"].textValue()
        val discordMaxShards = payload["discordMaxShards"].intValue()
        val discordShardMin = payload["discordShardMin"].intValue()
        val discordShardMax = payload["discordShardMax"].intValue()

        controller.discord.registerShard(
            socketWrapper,
            lorittaShardId,
            lorittaShardName,
            discordMaxShards,
            discordShardMin,
            discordShardMax
        )

        return JsonNodeFactory.instance.objectNode()
            .put("status", "OK") // TODO: Return IDENTIFY info (all connected shards atm, etc)
    }
}