package net.perfectdreams.loritta.cinnamon.platform.webserver

import io.ktor.client.*
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.locale.LorittaLanguageManager
import net.perfectdreams.loritta.cinnamon.common.utils.config.ConfigUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.metrics.Prometheus
import net.perfectdreams.loritta.cinnamon.platform.webserver.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import java.util.*
import kotlin.concurrent.thread

object LorittaCinnamonWebServerLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        // https://github.com/JetBrains/Exposed/issues/1356
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(LorittaCinnamonWebServer::class, ConfigUtils.defaultConfigFileName)
        logger.info { "Loaded Loritta's configuration file" }

        Prometheus.register()
        logger.info { "Registered Prometheus Metrics" }

        val languageManager = LorittaLanguageManager(LorittaCinnamonWebServer::class)

        val http = HttpClient {
            expectSuccess = false
        }

        val services = Pudding.createPostgreSQLPudding(
            rootConfig.services.pudding.address,
            rootConfig.services.pudding.database,
            rootConfig.services.pudding.username,
            rootConfig.services.pudding.password
        )
        services.setupShutdownHook()

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