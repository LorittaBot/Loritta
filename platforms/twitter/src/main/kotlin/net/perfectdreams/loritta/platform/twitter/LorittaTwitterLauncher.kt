package net.perfectdreams.loritta.platform.twitter

import mu.KotlinLogging
import net.perfectdreams.loritta.common.utils.config.ConfigUtils
import net.perfectdreams.loritta.platform.twitter.utils.config.RootConfig

object LorittaTwitterLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(LorittaTwitter::class, "loritta.conf")
        logger.info { "Loaded Loritta's configuration file" }

        val loritta = LorittaTwitter(
            rootConfig.loritta,
            rootConfig.twitter,
            rootConfig.services.gabrielaImageServer
        )

        loritta.start()
    }
}