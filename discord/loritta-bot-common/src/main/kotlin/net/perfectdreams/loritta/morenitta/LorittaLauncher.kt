package net.perfectdreams.loritta.morenitta

import net.perfectdreams.loritta.morenitta.utils.config.GeneralConfig
import net.perfectdreams.loritta.morenitta.utils.config.GeneralDiscordConfig
import net.perfectdreams.loritta.morenitta.utils.config.GeneralDiscordInstanceConfig
import net.perfectdreams.loritta.morenitta.utils.config.GeneralInstanceConfig
import kotlinx.coroutines.debug.DebugProbes
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Paths
import java.util.*
import java.util.jar.Attributes
import java.util.jar.JarFile

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
		try {
			setKotlinScriptingClasspath()
		} catch (e: FileNotFoundException) {
			println("Exception while trying to set Kotlin Scripting's classpath, are you running Loritta within a IDE?")
		}

		val configurationFile = File(System.getProperty("conf") ?: "./loritta.conf")
		val discordConfigurationFile = File(System.getProperty("discordConf") ?: "./discord.conf")
		val configurationInstanceFile = File(System.getProperty("instanceConf") ?: "./loritta.instance.conf")
		val discordInstanceConfigurationFile = File(System.getProperty("discordConf") ?: "./discord.instance.conf")

		if (!configurationFile.exists() || !discordConfigurationFile.exists()) {
			println("Welcome to Loritta Morenitta! :3")
			println("")
			println("I want to make the world a better place... helping people, making them laugh... I hope I succeed!")
			println("")
			println("Before we start, you will need to configure me.")
			println("I will create a file named \"loritta.conf\" (general configuration) and \"discord.conf\" (platform specific configuration), open it on your favorite text editor and change it!")
			println("")
			println("After configuring the file, run me again!")

			copyFromJar("/loritta.conf", "./loritta.conf")
			copyFromJar("/loritta.instance.conf", "./loritta.instance.conf")
			copyFromJar("/discord.conf", "./discord.conf")
			copyFromJar("/discord.instance.conf", "./discord.instance.conf")
			copyFromJar("/emotes.conf", "./emotes.conf")

			System.exit(1)
			return
		}

		val config = readConfigurationFromFile<GeneralConfig>(configurationFile)
		val discordConfig = readConfigurationFromFile<GeneralDiscordConfig>(discordConfigurationFile)
		val instanceConfig = readConfigurationFromFile<GeneralInstanceConfig>(configurationInstanceFile)
		val discordInstanceConfig = readConfigurationFromFile<GeneralDiscordInstanceConfig>(discordInstanceConfigurationFile)

		val services = Pudding.createPostgreSQLPudding(
			config.database.address,
			config.database.databaseName,
			config.database.username,
			config.database.password
		)
		services.setupShutdownHook()

		logger.info { "Started Pudding client!" }

		val jedisPoolConfig = JedisPoolConfig()
		jedisPoolConfig.maxTotal = 10

		val jedisPool = JedisPool(
			jedisPoolConfig,
			config.redis.address.substringBefore(":"),
			config.redis.address.substringAfter(":").toIntOrNull() ?: 6379,
			null,
			config.redis.password
		)

		// Used for Logback
		System.setProperty("cluster.name", config.clusters.first { it.id == instanceConfig.loritta.currentClusterId }.getUserAgent(config.loritta.environment))

		// Iniciar instância da Loritta
		val loritta = LorittaBot(discordConfig, discordInstanceConfig, config, instanceConfig, services, jedisPool)
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
