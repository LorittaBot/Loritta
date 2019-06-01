package net.perfectdreams.loritta.socket.network.commands

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.perfectdreams.loritta.platform.discord.entities.jda.JDAGuild
import net.perfectdreams.loritta.platform.network.discord.entities.DiscordNetworkGuild
import net.perfectdreams.loritta.socket.SocketWrapper
import net.perfectdreams.loritta.socket.network.SocketOpCode

class GetGuildsByIdCommand : SocketCommand(SocketOpCode.Discord.GET_GUILDS_BY_ID) {
    override suspend fun process(socketWrapper: SocketWrapper, payload: JsonNode): JsonNode {
        val guildIds = payload["guildIds"]

        val guilds = guildIds.asSequence()
                .mapNotNull {
                    lorittaShards.getGuildById(it.textValue())
                }
                .map {
                    JDAGuild(it)
                }
                .toList()

        val objNode = JsonNodeFactory.instance.objectNode()

        objNode["guilds"] = JsonNodeFactory.instance.arrayNode().apply {
            guilds.forEach {
                this.add(
                        DiscordNetworkGuild.toObjectNode(it)
                )
            }
        }

        return objNode
    }
}