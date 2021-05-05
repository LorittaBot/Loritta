package net.perfectdreams.loritta.platform.twitter

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.twitter.utils.config.RootConfig
import java.io.File

object LorittaTwitterLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        val rootConfig = loadConfig()
        logger.info { "Loaded Loritta's configuration file" }

        val loritta = LorittaTwitter(
            rootConfig.loritta,
            rootConfig.twitter,
            rootConfig.services.gabrielaImageServer
        )

        loritta.start()
    }

    private fun loadConfig(): RootConfig {
        val fileConfig = ConfigFactory.parseFile(File("./loritta.conf"))
        return Hocon.decodeFromConfig(fileConfig)
    }
}