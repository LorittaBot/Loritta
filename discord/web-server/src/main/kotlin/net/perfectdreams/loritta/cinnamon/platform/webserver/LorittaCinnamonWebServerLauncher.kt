package net.perfectdreams.loritta.cinnamon.platform.webserver

import io.ktor.client.*
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.common.utils.config.ConfigUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.metrics.Prometheus
import net.perfectdreams.loritta.cinnamon.platform.webserver.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import kotlin.concurrent.thread

object LorittaCinnamonWebServerLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(LorittaCinnamonWebServer::class, ConfigUtils.defaultConfigFileName)
        logger.info { "Loaded Loritta's configuration file" }

        Prometheus.register()
        logger.info { "Registered Prometheus Metrics" }

        val languageManager = LanguageManager(
            LorittaCinnamonWebServer::class,
            "en",
            "/languages/"
        )
        languageManager.loadLanguagesAndContexts()

        val http = HttpClient {
            expectSuccess = false
        }

        val services = Pudding.createPostgreSQLPudding(
            rootConfig.services.pudding.address,
            rootConfig.services.pudding.database,
            rootConfig.services.pudding.username,
            rootConfig.services.pudding.password
        )

        Runtime.getRuntime().addShutdownHook(
            thread(false) {
                // Shutdown services when stopping the application
                // This is needed for the Pudding Tasks
                services.shutdown()
            }
        )

        logger.info { "Started Pudding client!" }

        val loritta = LorittaCinnamonWebServer(
            rootConfig.loritta,
            rootConfig.discord,
            rootConfig.interactions,
            rootConfig.interactionsEndpoint,
            rootConfig.services,
            languageManager,
            services,
            http
        )

        loritta.start()
    }
}