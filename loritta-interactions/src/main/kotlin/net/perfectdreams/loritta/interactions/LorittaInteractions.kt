package net.perfectdreams.loritta.interactions

import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import net.perfectdreams.discordinteraktions.InteractionsServer
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.platform.PlatformFeature
import net.perfectdreams.loritta.api.plugin.PluginManager
import net.perfectdreams.loritta.api.utils.LorittaAssets
import net.perfectdreams.loritta.commands.vanilla.`fun`.ChooseCommand
import net.perfectdreams.loritta.commands.vanilla.`fun`.CoinFlipCommand
import net.perfectdreams.loritta.commands.vanilla.`fun`.MagicBallCommand
import net.perfectdreams.loritta.commands.vanilla.`fun`.QualityCommand
import net.perfectdreams.loritta.commands.vanilla.`fun`.RateWaifuCommand
import net.perfectdreams.loritta.commands.vanilla.`fun`.VaporQualityCommand
import net.perfectdreams.loritta.commands.vanilla.`fun`.VaporwaveCommand
import net.perfectdreams.loritta.commands.vanilla.utils.AnagramCommand
import net.perfectdreams.loritta.commands.vanilla.utils.CalculatorCommand
import net.perfectdreams.loritta.interactions.commands.vanilla.InteractionsCommandManager
import net.perfectdreams.loritta.interactions.commands.vanilla.PingAnotherThingCommand
import net.perfectdreams.loritta.interactions.commands.vanilla.PingSomethingCommand
import net.perfectdreams.loritta.interactions.utils.config.DiscordConfig
import net.perfectdreams.loritta.utils.locale.LocaleManager
import java.io.File
import kotlin.random.Random

class LorittaInteractions(val discordConfig: DiscordConfig) : LorittaBot() {
    val server = InteractionsServer(
        discordConfig.applicationId,
        discordConfig.publicKey,
        discordConfig.token
    )
    val localeManager = LocaleManager(File("L:\\RandomProjects\\LorittaInteractions\\locales"))
    override val commandManager = InteractionsCommandManager(this)

    override val supportedFeatures: List<PlatformFeature>
        get() = TODO("Not yet implemented")
    override val pluginManager: PluginManager
        get() = TODO("Not yet implemented")
    override val assets: LorittaAssets
        get() = TODO("Not yet implemented")
    override val http: HttpClient
        get() = TODO("Not yet implemented")
    override val httpWithoutTimeout: HttpClient
        get() = TODO("Not yet implemented")
    override val random = Random(System.currentTimeMillis())

    fun start() {
        localeManager.loadLocales()

        runBlocking {
            commandManager.registerAll(
                PingSomethingCommand(),
                PingAnotherThingCommand(),
                CalculatorCommand(),
                CoinFlipCommand(this@LorittaInteractions),
                RateWaifuCommand(this@LorittaInteractions),
                MagicBallCommand(this@LorittaInteractions),
                VaporwaveCommand(this@LorittaInteractions),
                QualityCommand(this@LorittaInteractions),
                VaporQualityCommand(this@LorittaInteractions),
                ChooseCommand(this@LorittaInteractions),
                AnagramCommand(this@LorittaInteractions)
            )

            commandManager.registerDiscord()

            server.start()
        }
    }
}