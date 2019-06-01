package net.perfectdreams.loritta.socket.network.commands

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.perfectdreams.loritta.platform.discord.entities.jda.JDAUser
import net.perfectdreams.loritta.platform.network.discord.entities.DiscordNetworkUser
import net.perfectdreams.loritta.socket.SocketWrapper
import net.perfectdreams.loritta.socket.network.SocketOpCode

class GetUsersByIdCommand : SocketCommand(SocketOpCode.Discord.GET_USERS_BY_ID) {
    override suspend fun process(socketWrapper: SocketWrapper, payload: JsonNode): JsonNode {
        val userIds = payload["userIds"]

        val users = userIds.asSequence()
                .mapNotNull {
                    lorittaShards.getUserById(it.textValue())
                }
                .map {
                    JDAUser(it)
                }
                .toList()

        val objNode = JsonNodeFactory.instance.objectNode()

        objNode["users"] = JsonNodeFactory.instance.arrayNode().apply {
            users.forEach {
                this.add(
                        DiscordNetworkUser.toObjectNode(it)
                )
            }
        }

        return objNode
    }
}