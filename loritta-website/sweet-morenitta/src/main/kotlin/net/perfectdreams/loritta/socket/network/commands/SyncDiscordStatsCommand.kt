package net.perfectdreams.loritta.socket.network.commands

import com.fasterxml.jackson.databind.JsonNode
import net.perfectdreams.loritta.socket.LorittaController
import net.perfectdreams.loritta.socket.SocketWrapper
import net.perfectdreams.loritta.socket.network.SocketOpCode
import net.perfectdreams.loritta.utils.extensions.objectNode

class SyncDiscordStatsCommand(val controller: LorittaController) : SocketCommand(SocketOpCode.Discord.UPDATE_DISCORD_STATS) {
    override suspend fun process(socketWrapper: SocketWrapper, payload: JsonNode): JsonNode {
        controller.discord.getDiscordShardFromSocketWrapper(socketWrapper).updateStats(payload)
        return objectNode()
    }
}