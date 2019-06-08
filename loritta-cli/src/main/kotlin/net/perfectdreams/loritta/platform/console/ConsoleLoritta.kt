package net.perfectdreams.loritta.platform.console

import com.fasterxml.jackson.module.kotlin.readValue
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.config.GeneralConfig
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.api.platform.LorittaBot
import net.perfectdreams.loritta.api.platform.PlatformFeature
import net.perfectdreams.loritta.platform.console.commands.ConsoleCommandManager
import net.perfectdreams.loritta.utils.Emotes
import java.io.File
import kotlin.concurrent.thread

/**
 * A [LorittaBot] implementation as a CLI tool
 */
class ConsoleLoritta(config: GeneralConfig) : LorittaBot(config) {
    override val supportedFeatures = listOf<PlatformFeature>()
    override val commandManager = ConsoleCommandManager(this)

    init {
        println("Init stuff...")

        Loritta.ASSETS = config.loritta.folders.assets

        Emotes.emoteManager = Emotes.EmoteManager.DefaultEmoteManager()

        loadLocales()
        loadLegacyLocales()

        println("Initialized!")
    }

    fun start() {
        println("Loading plugins...")
        pluginManager.loadPlugins()

        println("Setting up input thread...")
        thread {
            println("Input thread initialized!")
            while (true) {
                val line = readLine()!!
                println(line)
                runBlocking {
                    commandManager.dispatch(
                            line,
                            locales.getValue("default"),
                            LegacyBaseLocale()
                    )
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val configurationFile = File(System.getProperty("conf") ?: "./loritta.conf")
            val json = configurationFile.readText()
            val config = Constants.HOCON_MAPPER.readValue<GeneralConfig>(json)

            val loritta = ConsoleLoritta(config)
            loritta.start()
        }
    }
}