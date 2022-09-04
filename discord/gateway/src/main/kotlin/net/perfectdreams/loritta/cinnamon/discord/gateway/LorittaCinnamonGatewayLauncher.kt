package net.perfectdreams.loritta.cinnamon.discord.gateway

import io.ktor.client.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.debug.DebugProbes
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.gateway.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.discord.utils.RedisKeys
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.InteractionsMetrics
import net.perfectdreams.loritta.cinnamon.locale.LorittaLanguageManager
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.utils.config.ConfigUtils
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.util.*

object LorittaCinnamonGatewayLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        // https://github.com/JetBrains/Exposed/issues/1356
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        installCoroutinesDebugProbes()

        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(LorittaCinnamonGateway::class, ConfigUtils.defaultConfigFileName)
        logger.info { "Loaded Loritta's configuration file" }

        InteractionsMetrics.registerJFRExports()
        InteractionsMetrics.registerInteractions()

        logger.info { "Registered Prometheus Metrics" }

        val languageManager = LorittaLanguageManager(LorittaCinnamonGateway::class)

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
        jedisPoolConfig.maxTotal = 100
        // No reason to keep disconnecting and reconnecting from Jedis, let's keep a constant connection pool (like how HikariCP works)
        jedisPoolConfig.minIdle = jedisPoolConfig.maxTotal
        jedisPoolConfig.maxIdle = jedisPoolConfig.maxIdle

        val jedisPool = JedisPool(
            jedisPoolConfig,
            rootConfig.cinnamon.services.redis.address.substringBefore(":"),
            rootConfig.cinnamon.services.redis.address.substringAfter(":").toIntOrNull() ?: 6379,
            // The default timeout is 2_000, which, in my experience, was causing issues where connections couldn't be created, or
            // "Failed to connect to any host resolved for DNS name." .. "Suppressed: java.net.SocketTimeoutException: Connect timed out"
            // https://gist.github.com/JonCole/925630df72be1351b21440625ff2671f
            5_000,
            null,
            rootConfig.cinnamon.services.redis.password
        )

        val loritta = LorittaCinnamonGateway(
            rootConfig,
            languageManager,
            services,
            jedisPool,
            RedisKeys(rootConfig.cinnamon.services.redis.keyPrefix),
            http
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