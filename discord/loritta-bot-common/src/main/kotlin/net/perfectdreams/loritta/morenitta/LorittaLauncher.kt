package net.perfectdreams.loritta.morenitta

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.debug.DebugProbes
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.utils.RedisKeys
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.InteractionsMetrics
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.common.locale.LocaleManager
import net.perfectdreams.loritta.common.locale.LorittaLanguageManager
import net.perfectdreams.loritta.common.utils.HostnameUtils
import net.perfectdreams.loritta.morenitta.utils.config.BaseConfig
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.io.File
import java.util.*
import javax.imageio.ImageIO

/**
 * Loritta's Launcher
 *
 * @author MrPowerGamerBR
 */
object LorittaLauncher {
	private val logger = KotlinLogging.logger {}

	@JvmStatic
	fun main(args: Array<String>) {
		// https://github.com/JetBrains/Exposed/issues/1356
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
		installCoroutinesDebugProbes()

		// Speeds up image loading/writing/etc
		// https://stackoverflow.com/a/44170254/7271796
		ImageIO.setUseCache(false)

		val configurationFile = File(System.getProperty("conf") ?: "./loritta.conf")

		if (!configurationFile.exists()) {
			println("Welcome to Loritta Morenitta! :3")
			println("")
			println("I want to make the world a better place... helping people, making them laugh... I hope I succeed!")
			println("")
			println("Before we start, you need to configure me!")
			println("I created a file named \"loritta.conf\", there you can configure a lot of things and stuff related to me, open it on your favorite text editor and change it!")
			println("")
			println("After configuring the file, run me again!")

			copyFromJar("/loritta.conf", "./loritta.conf")
			copyFromJar("/emotes.conf", "./emotes.conf")

			System.exit(1)
			return
		}

		val config = readConfigurationFromFile<BaseConfig>(configurationFile)
		logger.info { "Loaded Loritta's configuration file" }

		val clusterId = if (config.loritta.clusters.getClusterIdFromHostname) {
			val hostname = HostnameUtils.getHostname()
			hostname.substringAfterLast("-").toIntOrNull() ?: error("Clusters are enabled, but I couldn't get the Cluster ID from the hostname!")
		} else {
			config.loritta.clusters.clusterIdOverride ?: 1
		}

		logger.info { "Loritta's Cluster ID: $clusterId" }

		InteractionsMetrics.registerJFRExports()
		InteractionsMetrics.registerInteractions()

		logger.info { "Registered Prometheus Metrics" }

		logger.info { "Loading languages..." }
		val languageManager = LorittaLanguageManager(LorittaBot::class)
		val localeManager = LocaleManager(LorittaBot::class).also { it.loadLocales() }

		val services = Pudding.createPostgreSQLPudding(
			config.loritta.pudding.address,
			config.loritta.pudding.database,
			config.loritta.pudding.username,
			config.loritta.pudding.password
		)
		services.setupShutdownHook()

		logger.info { "Started Pudding client!" }

		val jedisPoolConfig = JedisPoolConfig()
		jedisPoolConfig.maxTotal = 1_000

		val jedisPool = JedisPool(
			jedisPoolConfig,
			config.loritta.redis.address.substringBefore(":"),
			config.loritta.redis.address.substringAfter(":").toIntOrNull() ?: 6379,
			// The default timeout is 2_000, which, in my experience, was causing issues where connections couldn't be created, or
			// "Failed to connect to any host resolved for DNS name." .. "Suppressed: java.net.SocketTimeoutException: Connect timed out"
			// The default timeout may also cause issues when trying to read too many data from Redis (read timeout)
			// https://gist.github.com/JonCole/925630df72be1351b21440625ff2671f
			15_000,
			null,
			config.loritta.redis.password
		)

		// Used for Logback
		System.setProperty("cluster.name", config.loritta.clusters.instances.first { it.id == clusterId }.getUserAgent(config.loritta.environment))

		// Iniciar instância da Loritta
		val loritta = LorittaBot(clusterId, config, languageManager, localeManager, services, jedisPool, RedisKeys(config.loritta.redis.keyPrefix))
		loritta.start()
	}

	private fun copyFromJar(inputPath: String, outputPath: String) {
		val inputStream = LorittaLauncher::class.java.getResourceAsStream(inputPath)
		File(outputPath).writeBytes(inputStream.readAllBytes())
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	private fun installCoroutinesDebugProbes() {
		// It is recommended to set this to false to avoid performance hits with the DebugProbes option!
		DebugProbes.enableCreationStackTraces = false
		DebugProbes.install()
	}
}
