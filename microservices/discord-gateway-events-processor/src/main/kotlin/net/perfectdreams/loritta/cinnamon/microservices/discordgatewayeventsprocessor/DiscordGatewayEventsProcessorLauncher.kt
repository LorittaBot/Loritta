package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.debug.DebugProbes
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.locale.LorittaLanguageManager
import net.perfectdreams.loritta.cinnamon.common.utils.config.ConfigUtils
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import java.util.*
import java.util.concurrent.TimeUnit

object DiscordGatewayEventsProcessorLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        // https://github.com/JetBrains/Exposed/issues/1356
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(
            DiscordGatewayEventsProcessorLauncher::class, System.getProperty("discordgatewayeventsprocessor.config", "discord-gateway-events-processor.conf"))
        logger.info { "Loaded Loritta's configuration file" }

        val replicaId = if (rootConfig.replicas.getReplicaIdFromHostname) {
            val hostname = getHostname()
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

    private fun getHostname(): String {
        // From hostname command
        try {
            val proc = ProcessBuilder("hostname")
                .start()

            proc.waitFor(5, TimeUnit.SECONDS)
            val hostname = proc.inputStream.readAllBytes().toString(Charsets.UTF_8).removeSuffix("\n")
            proc.destroyForcibly()

            logger.warn { "Machine Hostname via \"hostname\" command: $hostname" }
            return hostname
        } catch (e: Exception) {
            logger.warn(e) { "Something went wrong while trying to get the machine's hostname via the \"hostname\" command!" }
        }

        // From hostname env variable
        System.getenv("HOSTNAME")?.let {
            logger.warn { "Machine Hostname via \"HOSTNAME\" env variable: $it" }
            return it
        }

        // From computername env variable
        System.getenv("COMPUTERNAME")?.let {
            logger.warn { "Machine Hostname via \"COMPUTERNAME\" env variable: $it" }
            return it
        }

        logger.warn { "I wasn't able to get the machine's hostname! Falling back to \"Unknown\"..." }
        return "Unknown"
    }
}