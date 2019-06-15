package net.perfectdreams.loritta.socket

import mu.KotlinLogging
import net.perfectdreams.loritta.socket.network.commands.IdentifyCommand
import net.perfectdreams.loritta.socket.network.commands.LorittaShardsCommand
import net.perfectdreams.loritta.socket.network.commands.SyncDiscordStatsCommand
import net.perfectdreams.loritta.utils.extensions.obj
import java.util.*

class LorittaController(port: Int) {
    private companion object {
        val logger = KotlinLogging.logger {}
    }

    val socketServer = LorittaSocketServer(port)
    val discord = DiscordController(this)

    fun start() {
        socketServer.registerCommands(
            IdentifyCommand(this),
            SyncDiscordStatsCommand(this),
            LorittaShardsCommand(this)
        )

        socketServer.onSocketDisconnect = { disconnectedSocket ->
            val disconnectedShard = discord.discordShards.values.firstOrNull { it.socketWrapper.socket == disconnectedSocket }

            if (disconnectedShard == null) {
                logger.warn("A socket without an identified shard was disconnected! Bug?")
            } else {
                logger.info("Loritta Discord Shard ${disconnectedShard.lorittaShardId} was disconnected!")
                disconnectedShard.socketWrapper.close()
                discord.discordShards.remove(disconnectedShard.lorittaShardId.toInt())
            }
        }

        socketServer.onMessageReceived = { node ->
            val uniqueId = UUID.fromString(node["uniqueId"].textValue())
            discord.discordShards.values.onEach {
                val request = it.socketWrapper._requests.getIfPresent(uniqueId) ?: return@onEach

                it.socketWrapper._requests.invalidate(uniqueId)
                request.first.invoke(node.obj)
            }
        }

        socketServer.start()
    }
}