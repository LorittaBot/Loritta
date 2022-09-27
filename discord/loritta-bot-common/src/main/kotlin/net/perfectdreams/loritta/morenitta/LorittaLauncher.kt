package net.perfectdreams.loritta.morenitta

import kotlinx.coroutines.debug.DebugProbes
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.utils.RedisKeys
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.InteractionsMetrics
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.common.locale.LorittaLanguageManager
import net.perfectdreams.loritta.common.utils.HostnameUtils
import net.perfectdreams.loritta.morenitta.utils.newconfig.BaseConfig
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Paths
import java.util.*
import java.util.jar.Attributes
import java.util.jar.JarFile
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

		try {
			setKotlinScriptingClasspath()
		} catch (e: FileNotFoundException) {
			println("Exception while trying to set Kotlin Scripting's classpath, are you running Loritta within a IDE?")
		}

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

		val languageManager = LorittaLanguageManager(LorittaBot::class)

		val services = Pudding.createPostgreSQLPudding(
			config.loritta.pudding.address,
			config.loritta.pudding.database,
			config.loritta.pudding.username,
			config.loritta.pudding.password
		)
		services.setupShutdownHook()

		logger.info { "Started Pudding client!" }

		val jedisPoolConfig = JedisPoolConfig()
		jedisPoolConfig.maxTotal = 10

		val jedisPool = JedisPool(
			jedisPoolConfig,
			config.loritta.redis.address.substringBefore(":"),
			config.loritta.redis.address.substringAfter(":").toIntOrNull() ?: 6379,
			null,
			config.loritta.redis.password
		)

		// Used for Logback
		System.setProperty("cluster.name", config.loritta.clusters.instances.first { it.id == clusterId }.getUserAgent(config.loritta.environment))

		// Iniciar instância da Loritta
		val loritta = LorittaBot(clusterId, config, languageManager, services, jedisPool, RedisKeys(config.loritta.redis.keyPrefix))
		loritta.start()
	}

	private fun copyFromJar(inputPath: String, outputPath: String) {
		val inputStream = LorittaLauncher::class.java.getResourceAsStream(inputPath)
		File(outputPath).writeBytes(inputStream.readAllBytes())
	}

	private fun setKotlinScriptingClasspath() {
		// https://www.reddit.com/r/Kotlin/comments/8qdd4x/kotlin_script_engine_and_your_classpaths_what/
		val path = this::class.java.protectionDomain.codeSource.location.path
		val jar = JarFile(path)
		val mf = jar.manifest
		val mattr = mf.mainAttributes
		// Yes, you SHOULD USE Attributes.Name.CLASS_PATH! Don't try using "Class-Path", it won't work!
		val manifestClassPath = mattr[Attributes.Name.CLASS_PATH] as String

		// The format within the Class-Path attribute is different than the one expected by the property, so let's fix it!
		// By the way, don't forget to append your original JAR at the end of the string!
		val clazz = LorittaLauncher::class.java
		val protectionDomain = clazz.protectionDomain
		val propClassPath = manifestClassPath.replace(" ", File.pathSeparator) + "${File.pathSeparator}${Paths.get(protectionDomain.codeSource.location.toURI()).fileName}"

		// Now we set it to our own classpath
		System.setProperty("kotlin.script.classpath", propClassPath)
	}

	private fun installCoroutinesDebugProbes() {
		// It is recommended to set this to false to avoid performance hits with the DebugProbes option!
		DebugProbes.enableCreationStackTraces = false
		DebugProbes.install()
	}
}
