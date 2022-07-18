package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.debug.DebugProbes
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.locale.LorittaLanguageManager
import net.perfectdreams.loritta.cinnamon.common.utils.HostnameUtils
import net.perfectdreams.loritta.cinnamon.common.utils.config.ConfigUtils
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.platform.utils.metrics.JFRExports
import net.perfectdreams.loritta.cinnamon.platform.utils.metrics.Prometheus
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import java.util.*
import java.util.concurrent.TimeUnit

object DiscordGatewayEventsProcessorLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        // https://github.com/JetBrains/Exposed/issues/1356
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        JFRExports.register()

        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(
            DiscordGatewayEventsProcessorLauncher::class, System.getProperty("discordgatewayeventsprocessor.config", "discord-gateway-events-processor.conf"))
        logger.info { "Loaded Loritta's configuration file" }

        val replicaId = if (rootConfig.replicas.getReplicaIdFromHostname) {
            val hostname = HostnameUtils.getHostname()
            hostname.substringAfterLast("-").toIntOrNull() ?: error("Replicas is enabled, but I couldn't get the Replica ID from the hostname!")
        } else {
            rootConfig.replicas.replicaIdOverride ?: 0
        }

        logger.info { "Replica ID: $replicaId" }

        val languageManager = LorittaLanguageManager(DiscordGatewayEventsProcessor::class)

        val services = Pudding.createPostgreSQLPudding(
            rootConfig.pudding.address,
            rootConfig.pudding.database,
            rootConfig.pudding.username,
            rootConfig.pudding.password
        )
        services.setupShutdownHook()

        logger.info { "Started Pudding client!" }

        installCoroutinesDebugProbes()

        val loritta = DiscordGatewayEventsProcessor(
            rootConfig,
            services,
            languageManager,
            replicaId
        )

        loritta.start()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun installCoroutinesDebugProbes() {
        // It is recommended to set this to false, to avoid performance hits with the DebugProbes option!
        DebugProbes.enableCreationStackTraces = false
        DebugProbes.install()
    }
}