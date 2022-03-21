package net.perfectdreams.loritta.cinnamon.microservices.statscollector

import io.ktor.client.*
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.utils.config.ConfigUtils
import net.perfectdreams.loritta.cinnamon.microservices.statscollector.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import java.util.*
import kotlin.concurrent.thread

object StatsCollectorLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        // https://github.com/JetBrains/Exposed/issues/1356
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(StatsCollectorLauncher::class, System.getProperty("statscollector.config", "stats-collector.conf"))
        logger.info { "Loaded Loritta's configuration file" }

        val http = HttpClient {
            expectSuccess = false
        }

        val services = Pudding.createPostgreSQLPudding(
            rootConfig.pudding.address,
            rootConfig.pudding.database,
            rootConfig.pudding.username,
            rootConfig.pudding.password
        )

        val loritta = StatsCollector(
            rootConfig,
            services,
            http
        )

        Runtime.getRuntime().addShutdownHook(
            thread(false) {
                // Shutdown services when stopping the application
                // This is needed for the Pudding Tasks
                services.shutdown()
            }
        )

        logger.info { "Started Pudding client!" }

        loritta.start()
    }
}