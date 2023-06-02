package net.perfectdreams.loritta.morenitta

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.debug.DebugProbes
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.InteractionsMetrics
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.common.locale.LocaleManager
import net.perfectdreams.loritta.common.locale.LorittaLanguageManager
import net.perfectdreams.loritta.common.utils.HostnameUtils
import net.perfectdreams.loritta.morenitta.utils.config.BaseConfig
import net.perfectdreams.loritta.morenitta.utils.devious.GatewaySessionData
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
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

		val lorittaCluster = config.loritta.clusters.instances.first { it.id == clusterId }
		logger.info { "Loritta's Cluster ID: $clusterId (${lorittaCluster.name})" }

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

		// Used for Logback
		System.setProperty("cluster.name", config.loritta.clusters.instances.first { it.id == clusterId }.getUserAgent(config.loritta.environment))

		val cacheFolder = File("cache")
		cacheFolder.mkdirs()

		val initialSessions = mutableMapOf<Int, GatewaySessionData>()
		val previousVersionKeyFile = File(cacheFolder, "version")
		if (previousVersionKeyFile.exists()) {
			val previousVersion = UUID.fromString(previousVersionKeyFile.readText())
			for (shard in lorittaCluster.minShard..lorittaCluster.maxShard) {
				try {
					val shardCacheFolder = File(cacheFolder, shard.toString())
					val sessionFile = File(shardCacheFolder, "session.json")
					val cacheVersionKeyFile = File(shardCacheFolder, "version")
					// Does not exist, so bail out
					if (!cacheVersionKeyFile.exists()) {
						logger.warn("Couldn't load shard $shard cached data because the version file does not exist!")
						continue
					}

					val cacheVersion = UUID.fromString(cacheVersionKeyFile.readText())
					// Only load the data if the version matches
					if (cacheVersion == previousVersion) {
						if (sessionFile.exists()) {
							val sessionData = if (sessionFile.exists()) Json.decodeFromString<GatewaySessionData>(sessionFile.readText()) else null
							if (sessionData != null)
								initialSessions[shard] = sessionData
						}
					} else {
						logger.warn { "Couldn't load shard $shard cached data because the cache version does not match!" }
					}
				} catch (e: Exception) {
					logger.warn { "Failed to load shard $shard cached data!" }
				}
			}
		}

		// Iniciar instância da Loritta
		val loritta = LorittaBot(clusterId, config, languageManager, localeManager, services, cacheFolder, initialSessions)
		loritta.start()
	}

	private fun copyFromJar(inputPath: String, outputPath: String) {
		val inputStream = LorittaLauncher::class.java.getResourceAsStream(inputPath)
		File(outputPath).writeBytes(inputStream.readAllBytes())
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	private fun installCoroutinesDebugProbes() {
		// Enable coroutine names, they are visible when dumping the coroutines
		System.setProperty("kotlinx.coroutines.debug", "on")

		// Enable coroutines stacktrace recovery
		System.setProperty("kotlinx.coroutines.stacktrace.recovery", "true")

		// It is recommended to set this to false to avoid performance hits with the DebugProbes option!
		DebugProbes.enableCreationStackTraces = false
		DebugProbes.install()
	}
}
