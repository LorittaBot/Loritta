package net.perfectdreams.loritta.cinnamon.discord.webserver

import io.ktor.client.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.debug.DebugProbes
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.utils.RedisKeys
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.InteractionsMetrics
import net.perfectdreams.loritta.cinnamon.discord.webserver.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.locale.LorittaLanguageManager
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.utils.HostnameUtils
import net.perfectdreams.loritta.cinnamon.utils.config.ConfigUtils
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.util.*

object LorittaCinnamonWebServerLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        // https://github.com/JetBrains/Exposed/issues/1356
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        installCoroutinesDebugProbes()

        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(LorittaCinnamonWebServer::class, ConfigUtils.defaultConfigFileName)
        logger.info { "Loaded Loritta's configuration file" }

        val replicaId = if (rootConfig.replicas.getReplicaIdFromHostname) {
            val hostname = HostnameUtils.getHostname()
            hostname.substringAfterLast("-").toIntOrNull() ?: error("Replicas is enabled, but I couldn't get the Replica ID from the hostname!")
        } else {
            rootConfig.replicas.replicaIdOverride ?: 0
        }

        InteractionsMetrics.registerJFRExports()
        InteractionsMetrics.registerInteractions()

        logger.info { "Registered Prometheus Metrics" }

        val languageManager = LorittaLanguageManager(LorittaCinnamonWebServer::class)

        val http = HttpClient {
            expectSuccess = false
        }

        val services = Pudding.createPostgreSQLPudding(
            rootConfig.cinnamon.services.pudding.address,
            rootConfig.cinnamon.services.pudding.database,
            rootConfig.cinnamon.services.pudding.username,
            rootConfig.cinnamon.services.pudding.password
        )
        services.setupShutdownHook()

        logger.info { "Started Pudding client!" }

        val jedisPoolConfig = JedisPoolConfig()
        jedisPoolConfig.maxTotal = 25

        val jedisPool = JedisPool(
            jedisPoolConfig,
            rootConfig.cinnamon.services.redis.address.substringBefore(":"),
            rootConfig.cinnamon.services.redis.address.substringAfter(":").toIntOrNull() ?: 6379,
            null,
            rootConfig.cinnamon.services.redis.address.substringBefore("@").ifEmpty { null }
        )

        val loritta = LorittaCinnamonWebServer(
            rootConfig,
            languageManager,
            services,
            jedisPool,
            RedisKeys(rootConfig.cinnamon.services.redis.keyPrefix),
            http,
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