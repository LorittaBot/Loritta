package net.perfectdreams.loritta.cinnamon.dashboard.backend

import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.locale.LorittaLanguageManager
import net.perfectdreams.loritta.cinnamon.common.utils.config.ConfigUtils
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import java.util.*

object LorittaDashboardBackendLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        // https://github.com/JetBrains/Exposed/issues/1356
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(LorittaDashboardBackend::class, System.getProperty("spicymorenitta.config", "spicy-morenitta.conf"))
        logger.info { "Loaded SpicyMorenitta's configuration file" }

        val languageManager = LorittaLanguageManager(LorittaDashboardBackend::class)

        val services = Pudding.createPostgreSQLPudding(
            rootConfig.pudding.address,
            rootConfig.pudding.database,
            rootConfig.pudding.username,
            rootConfig.pudding.password
        )
        services.setupShutdownHook()

        logger.info { "Started Pudding client!" }

        val m = LorittaDashboardBackend(rootConfig, languageManager, services)
        m.start()
    }
}