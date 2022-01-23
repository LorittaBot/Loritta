package net.perfectdreams.loritta.cinnamon.microservices.statscollector

import io.ktor.client.*
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.utils.config.ConfigUtils
import net.perfectdreams.loritta.cinnamon.microservices.statscollector.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import kotlin.concurrent.thread

object StatsCollectorLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(StatsCollectorLauncher::class, System.getProperty("analyticscollector.config", "analytics-collector.conf"))
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
                loritta.shuttingDown = true
                loritta.jobs.forEach {
                    runBlocking {
                        try {
                            it.cancelAndJoin()
                        } catch (e: Exception) {}
                    }
                }

                // Shutdown services when stopping the application
                // This is needed for the Pudding Tasks
                services.shutdown()
            }
        )

        logger.info { "Started Pudding client!" }

        loritta.start()
    }
}