package net.perfectdreams.loritta.cinnamon.microservices.correiospackagetracker

import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.common.utils.config.ConfigUtils
import net.perfectdreams.loritta.cinnamon.microservices.correiospackagetracker.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import java.util.*
import kotlin.concurrent.thread

object CorreiosPackageTrackerLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        // https://github.com/JetBrains/Exposed/issues/1356
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(CorreiosPackageTrackerLauncher::class, System.getProperty("correiospackagetracker.config", "correios-package-tracker.conf"))
        logger.info { "Loaded Loritta's configuration file" }

        val languageManager = LanguageManager(
            CorreiosPackageTracker::class,
            "en",
            "/languages/"
        )
        languageManager.loadLanguagesAndContexts()

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

        val loritta = CorreiosPackageTracker(
            rootConfig,
            services,
            languageManager
        )

        loritta.start()
    }
}