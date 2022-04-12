package net.perfectdreams.loritta.cinnamon.microservices.directmessageprocessor

import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.utils.config.ConfigUtils
import net.perfectdreams.loritta.cinnamon.microservices.directmessageprocessor.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import java.util.*
import kotlin.concurrent.thread

object DiscordGatewayEventsProcessorLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        // https://github.com/JetBrains/Exposed/issues/1356
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(DiscordGatewayEventsProcessorLauncher::class, System.getProperty("directmessageprocessor.config", "direct-message-processor.conf"))
        logger.info { "Loaded Loritta's configuration file" }

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

        val loritta = DiscordGatewayEventsProcessor(
            rootConfig,
            services
        )

        loritta.start()
    }
}