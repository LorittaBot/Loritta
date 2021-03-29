package net.perfectdreams.loritta.interactions

import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.runBlocking
import net.perfectdreams.discordinteraktions.InteractionsServer
import net.perfectdreams.loritta.interactions.commands.vanilla.PingCommand
import net.perfectdreams.loritta.interactions.utils.config.DiscordConfig

class LorittaInteractions(val discordConfig: DiscordConfig) {
    val server = InteractionsServer(
        discordConfig.applicationId,
        discordConfig.publicKey,
        discordConfig.token
    )

    fun start() {
        runBlocking {
            server.commandManager.registerAll(
                PingCommand()
            )

            server.commandManager.updateAllCommandsInGuild(
                Snowflake(297732013006389252L),
                deleteUnknownCommands = true
            )

            server.start()
        }
    }
}