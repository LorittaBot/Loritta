package net.perfectdreams.loritta.cinnamon.platform.gateway

import io.ktor.client.*
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.common.utils.config.ConfigUtils
import net.perfectdreams.loritta.cinnamon.platform.gateway.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.platform.utils.metrics.Prometheus
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import kotlin.concurrent.thread

object LorittaCinnamonGatewayLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(LorittaCinnamonGateway::class, ConfigUtils.defaultConfigFileName)
        logger.info { "Loaded Loritta's configuration file" }

        Prometheus.register()
        logger.info { "Registered Prometheus Metrics" }

        val languageManager = LanguageManager(
            LorittaCinnamonGateway::class,
            "en",
            "/languages/"
        )
        languageManager.loadLanguagesAndContexts()

        val http = HttpClient {
            expectSuccess = false
        }

        val services = Pudding.createPostgreSQLPudding(
            rootConfig.services.pudding.address ?: error("Missing database address!"),
            rootConfig.services.pudding.database ?: error("Missing database!"),
            rootConfig.services.pudding.username ?: error("Missing database username!"),
            rootConfig.services.pudding.password ?: error("Missing database password!")
        )

        Runtime.getRuntime().addShutdownHook(
            thread(false) {
                // Shutdown services when stopping the application
                // This is needed for the Pudding Tasks
                services.shutdown()
            }
        )

        logger.info { "Started Pudding client!" }

        val loritta = LorittaCinnamonGateway(
            rootConfig.loritta,
            rootConfig.discord,
            rootConfig.interactions,
            rootConfig.services,
            languageManager,
            services,
            http
        )

        loritta.start()
    }
}