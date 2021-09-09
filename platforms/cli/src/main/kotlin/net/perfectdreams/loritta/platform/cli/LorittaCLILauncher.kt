package net.perfectdreams.loritta.cinnamon.platform.cli

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.utils.config.ConfigUtils
import net.perfectdreams.loritta.cinnamon.platform.cli.utils.config.RootConfig

object LorittaCLILauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(LorittaCLI::class, ConfigUtils.defaultConfigFileName)
        logger.info { "Loaded Loritta's configuration file" }

        val loritta = LorittaCLI(rootConfig.loritta, rootConfig.services.gabrielaImageServer)
        loritta.start()

        runBlocking {
            loritta.runArgs(args)
        }
    }
}