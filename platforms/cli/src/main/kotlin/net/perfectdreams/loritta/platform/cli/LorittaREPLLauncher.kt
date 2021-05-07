package net.perfectdreams.loritta.platform.cli

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.loritta.common.utils.config.ConfigUtils
import net.perfectdreams.loritta.platform.cli.utils.config.RootConfig

object LorittaREPLLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(LorittaCLI::class, ConfigUtils.defaultConfigFileName)
        logger.info { "Loaded Loritta's configuration file" }

        val loritta = LorittaREPL(rootConfig)

        runBlocking {
            loritta.start()
        }
    }
}