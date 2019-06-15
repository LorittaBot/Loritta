package net.perfectdreams.loritta.socket.network.commands

import com.fasterxml.jackson.databind.JsonNode
import net.perfectdreams.loritta.socket.LorittaController
import net.perfectdreams.loritta.socket.SocketWrapper
import net.perfectdreams.loritta.socket.network.SocketOpCode
import net.perfectdreams.loritta.utils.extensions.arrayNode
import net.perfectdreams.loritta.utils.extensions.objectNode
import net.perfectdreams.loritta.utils.extensions.toNodeArray

class LorittaShardsCommand(val controller: LorittaController) : SocketCommand(SocketOpCode.Discord.GET_LORITTA_SHARDS) {
    override suspend fun process(socketWrapper: SocketWrapper, payload: JsonNode): JsonNode {
        val array = arrayNode()

        for (shard in controller.discord.discordShards.values) {
            array.add(
                objectNode(
                    "lorittaShardId" to shard.lorittaShardId,
                    "lorittaShardName" to shard.lorittaShardName,
                    "discordMaxShards" to shard.discordShardMax,
                    "discordShardMin" to shard.discordShardMin,
                    "discordShardMax" to shard.discordShardMax,
                    "shards" to shard.shards.values.map {
                        objectNode(
                            "id" to it.id,
                            "guildCount" to it.guildCount,
                            "userCount" to it.userCount,
                            "gatewayPing" to it.gatewayPing,
                            "status" to it.status
                        )
                    }.toNodeArray()
                )
            )
        }

        return objectNode(
            "lorittaShards" to array
        )
    }
}