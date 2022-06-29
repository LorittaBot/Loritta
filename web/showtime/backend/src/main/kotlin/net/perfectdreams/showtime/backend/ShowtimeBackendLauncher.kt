package net.perfectdreams.showtime.backend

import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.locale.LorittaLanguageManager
import net.perfectdreams.loritta.cinnamon.common.utils.config.ConfigUtils
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.showtime.backend.utils.config.RootConfig
import java.util.*
import kotlin.concurrent.thread

object ShowtimeBackendLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        // https://github.com/JetBrains/Exposed/issues/1356
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(ShowtimeBackendLauncher::class, System.getProperty("showtime.config", "showtime.conf"))
        logger.info { "Loaded Showtime Backend's configuration file" }

        val languageManager = LorittaLanguageManager(ShowtimeBackendLauncher::class)

        val services = Pudding.createPostgreSQLPudding(
            rootConfig.pudding.address,
            rootConfig.pudding.database,
            rootConfig.pudding.username,
            rootConfig.pudding.password
        )

        Runtime.getRuntime().addShutdownHook(
            thread(false) {
                // Shutdown services when stopping the application
                // This is needed for the Pudding Tasks
                services.shutdown()
            }
        )

        logger.info { "Started Pudding client!" }

        val showtime = ShowtimeBackend(rootConfig, languageManager, services)
        showtime.start()
    }
}