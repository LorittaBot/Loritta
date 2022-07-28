package net.perfectdreams.loritta.cinnamon.microservices.directmessageprocessor

import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.locale.LorittaLanguageManager
import net.perfectdreams.loritta.cinnamon.utils.config.ConfigUtils
import net.perfectdreams.loritta.cinnamon.microservices.directmessageprocessor.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.InteractionsMetrics
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import java.util.*

object DirectMessageProcessorLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        // https://github.com/JetBrains/Exposed/issues/1356
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(DirectMessageProcessorLauncher::class, System.getProperty("directmessageprocessor.config", "direct-message-processor.conf"))
        logger.info { "Loaded Loritta's configuration file" }

        InteractionsMetrics.registerJFRExports()

        logger.info { "Registered Prometheus Metrics" }

        val languageManager = LorittaLanguageManager(DirectMessageProcessorLauncher::class)

        val services = Pudding.createPostgreSQLPudding(
            rootConfig.pudding.address,
            rootConfig.pudding.database,
            rootConfig.pudding.username,
            rootConfig.pudding.password
        )
        services.setupShutdownHook()

        logger.info { "Started Pudding client!" }

        val loritta = DirectMessageProcessor(
            rootConfig,
            languageManager,
            services
        )

        loritta.start()
    }
}