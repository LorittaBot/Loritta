package net.perfectdreams.loritta.platform.console

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.utils.ConnectionManager
import com.mrpowergamerbr.loritta.utils.config.GeneralConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralInstanceConfig
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.api.platform.LorittaBot
import net.perfectdreams.loritta.api.platform.PlatformFeature
import net.perfectdreams.loritta.platform.console.commands.ConsoleCommandManager
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.readConfigurationFromFile
import sun.misc.Unsafe
import java.io.File
import kotlin.concurrent.thread

/**
 * A [LorittaBot] implementation as a CLI tool
 */
class ConsoleLoritta(config: GeneralConfig, instanceConfig: GeneralInstanceConfig) : LorittaBot(config, instanceConfig) {
    override val supportedFeatures = listOf<PlatformFeature>()
    override val commandManager = ConsoleCommandManager(this)


    init {
        println("Init stuff...")

        val singleoneInstanceField = Unsafe::class.java!!.getDeclaredField("theUnsafe")
        singleoneInstanceField.setAccessible(true)
        val unsafe = singleoneInstanceField.get(null) as Unsafe
        // Hack para criar uma instância da Lori sem precisar criar um discord.conf
        // Já que algumas partes do código dela usam a "loritta"
        val hackLori = unsafe.allocateInstance(Loritta::class.java) as Loritta
        hackLori.config = config
        Loritta::class.java.getDeclaredField("connectionManager").let {
            it.isAccessible = true
            it.set(hackLori, ConnectionManager())
        }
        LorittaLauncher.loritta = hackLori

        Loritta.ASSETS = instanceConfig.loritta.folders.assets

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
            val config = readConfigurationFromFile<GeneralConfig>(File(System.getProperty("conf") ?: "./loritta.conf"))
            val instanceConfig = readConfigurationFromFile<GeneralInstanceConfig>(File(System.getProperty("conf") ?: "./loritta.instance.conf"))

            val loritta = ConsoleLoritta(config, instanceConfig)
            loritta.start()
        }
    }
}