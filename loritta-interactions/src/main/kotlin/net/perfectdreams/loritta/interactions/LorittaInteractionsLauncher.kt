package net.perfectdreams.loritta.interactions

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import java.io.File

object LorittaInteractionsLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val m = LorittaInteractions(loadConfig("./discord.conf"))
        m.start()
    }

    inline fun <reified T> loadConfig(path: String): T {
        // Getting Loritta Helper config file
        val lightbendConfig = ConfigFactory.parseFile(File(path))
            .resolve()

        // Parsing HOCON config
        return Hocon.decodeFromConfig(lightbendConfig)
    }

    inline fun <reified T> loadConfigOrNull(path: String): T? {
        val file = File(path)
        if (!file.exists())
            return null

        // Getting Loritta Helper config file
        val lightbendConfig = ConfigFactory.parseFile(File(path))
            .resolve()

        // Parsing HOCON config
        return Hocon.decodeFromConfig(lightbendConfig)
    }
}