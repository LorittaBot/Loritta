package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.debug.DebugProbes
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.locale.LorittaLanguageManager
import net.perfectdreams.loritta.cinnamon.utils.HostnameUtils
import net.perfectdreams.loritta.cinnamon.utils.config.ConfigUtils
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.JFRExports
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import java.util.*

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
        val guildCreateServices = Pudding.createPostgreSQLPudding(
            rootConfig.pudding.address,
            rootConfig.pudding.database,
            rootConfig.pudding.username,
            rootConfig.pudding.password,
            1024
        ) {
            // Because GuildCreate is mostly I/O bound, we want to have a lot of active but idle connections here
            // We also want to use a separate service, to avoid blocking any other events, due to the way GuildCreate creates a "perfect storm" when a
            // Loritta cluster restarts
            maximumPoolSize = 90
            poolName = "PuddingGuildCreatePool"
        }

        services.setupShutdownHook()

        logger.info { "Started Pudding client!" }

        installCoroutinesDebugProbes()

        val loritta = DiscordGatewayEventsProcessor(
            rootConfig,
            services,
            guildCreateServices,
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