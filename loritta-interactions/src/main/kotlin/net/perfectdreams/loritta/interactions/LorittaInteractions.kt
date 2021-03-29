package net.perfectdreams.loritta.interactions

import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.runBlocking
import net.perfectdreams.discordinteraktions.InteractionsServer
import net.perfectdreams.loritta.interactions.commands.vanilla.AnagramCommand
import net.perfectdreams.loritta.interactions.commands.vanilla.LocaleDebugCommand
import net.perfectdreams.loritta.interactions.commands.vanilla.PingCommand
import net.perfectdreams.loritta.interactions.utils.config.DiscordConfig
import net.perfectdreams.loritta.utils.locale.LocaleManager
import java.io.File

class LorittaInteractions(val discordConfig: DiscordConfig) {
    val server = InteractionsServer(
        discordConfig.applicationId,
        discordConfig.publicKey,
        discordConfig.token
    )
    val localeManager = LocaleManager(File("L:\\RandomProjects\\LorittaInteractions\\locales"))

    fun start() {
        localeManager.loadLocales()

        runBlocking {
            server.commandManager.registerAll(
                PingCommand(),
                AnagramCommand(),
                LocaleDebugCommand(this@LorittaInteractions)
            )

            server.commandManager.updateAllCommandsInGuild(
                Snowflake(297732013006389252L),
                deleteUnknownCommands = true
            )

            server.start()
        }
    }
}