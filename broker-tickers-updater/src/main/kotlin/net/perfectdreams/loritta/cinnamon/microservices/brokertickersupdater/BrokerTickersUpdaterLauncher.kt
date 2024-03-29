package net.perfectdreams.loritta.cinnamon.microservices.brokertickersupdater

import io.ktor.client.*
import mu.KotlinLogging
import net.perfectdreams.loritta.common.utils.config.ConfigUtils
import net.perfectdreams.loritta.cinnamon.microservices.brokertickersupdater.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import java.util.*

object BrokerTickersUpdaterLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        // https://github.com/JetBrains/Exposed/issues/1356
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(BrokerTickersUpdaterLauncher::class, System.getProperty("brokertickersupdater.config", "broker-tickers-updater.conf"))
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
        services.setupShutdownHook()

        logger.info { "Started Pudding client!" }

        val loritta = BrokerTickersUpdater(
            rootConfig,
            services,
            http
        )

        loritta.start()
    }
}