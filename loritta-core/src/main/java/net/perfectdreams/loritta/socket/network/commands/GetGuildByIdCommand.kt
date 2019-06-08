package net.perfectdreams.loritta.socket.network.commands

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.perfectdreams.loritta.platform.discord.entities.jda.JDAGuild
import net.perfectdreams.loritta.platform.network.discord.entities.DiscordNetworkGuild
import net.perfectdreams.loritta.socket.SocketWrapper
import net.perfectdreams.loritta.socket.network.SocketOpCode

class GetGuildByIdCommand : SocketCommand(SocketOpCode.Discord.GET_GUILD_BY_ID) {
    override suspend fun process(socketWrapper: SocketWrapper, payload: JsonNode): JsonNode {
        val guildId = payload["guildId"].textValue()

        val user = lorittaShards.getGuildById(guildId) ?: return JsonNodeFactory.instance.objectNode()
        val discordGuild = JDAGuild(user)

        val objNode = JsonNodeFactory.instance.objectNode()

        loritta.getServerConfigForGuild(guildId)

        objNode["guild"] = DiscordNetworkGuild.toObjectNode(discordGuild)

        return objNode
    }
}