package net.perfectdreams.loritta.socket.network.commands

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.perfectdreams.loritta.platform.discord.entities.jda.JDAUser
import net.perfectdreams.loritta.platform.network.discord.entities.DiscordNetworkUser
import net.perfectdreams.loritta.socket.SocketWrapper
import net.perfectdreams.loritta.socket.network.SocketOpCode
import net.perfectdreams.loritta.utils.extensions.set

class GetUserByIdCommand : SocketCommand(SocketOpCode.Discord.GET_USER_BY_ID) {
    override suspend fun process(socketWrapper: SocketWrapper, payload: JsonNode): JsonNode {
        val userId = payload["userId"].textValue()

        val user = lorittaShards.getUserById(userId) ?: return JsonNodeFactory.instance.objectNode()
        val discordUser = JDAUser(user)

        val objNode = JsonNodeFactory.instance.objectNode()

        // Não importa qual shard seja, desde que conecte na shard da Lori atual
        objNode["foundInShard"] = lorittaShards.shardManager.shards.first().shardInfo?.shardId ?: 0
        objNode["user"] = DiscordNetworkUser.toObjectNode(discordUser)

        return objNode
    }
}