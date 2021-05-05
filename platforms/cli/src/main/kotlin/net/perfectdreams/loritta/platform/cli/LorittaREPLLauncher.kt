package net.perfectdreams.loritta.platform.cli

import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.cli.utils.config.RootConfig
import java.io.File

object LorittaREPLLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        val rootConfig = loadConfig()
        logger.info { "Loaded Loritta's configuration file" }

        val loritta = LorittaREPL(rootConfig)

        runBlocking {
            loritta.start()
        }
    }

    private fun loadConfig(): RootConfig {
        val fileConfig = ConfigFactory.parseFile(File("./loritta.conf"))
        return Hocon.decodeFromConfig(fileConfig)
    }
}