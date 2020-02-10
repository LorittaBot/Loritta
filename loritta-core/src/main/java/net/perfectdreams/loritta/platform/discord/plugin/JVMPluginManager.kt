package net.perfectdreams.loritta.platform.discord.plugin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.readValue
import com.mrpowergamerbr.loritta.plugin.InvalidPluginException
import com.mrpowergamerbr.loritta.utils.Constants
import mu.KotlinLogging
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.plugin.LorittaPlugin
import net.perfectdreams.loritta.api.plugin.PluginManager
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.jar.JarFile

class JVMPluginManager(val loritta: LorittaDiscord) : PluginManager {
	companion object {
		val logger = KotlinLogging.logger {}
	}

	override val plugins = mutableListOf<LorittaPlugin>()

	override fun loadPlugin(plugin: LorittaPlugin) {
		logger.info { "Loading ${plugin.name}" }
		try {
			plugin.onEnable()
		} catch (e: Exception) {
			logger.error(e) { "Exception while enabling plugin ${plugin.name}" }
			unloadPlugin(plugin)
		}
	}

	override fun unloadPlugin(plugin: LorittaPlugin) {
		logger.info { "Disabling ${plugin.name}" }
		try {
			plugin.onDisable()
		} catch (e: Exception) {
			logger.error(e) { "Exception while disabling plugin ${plugin.name}" }
		}

		loritta.commandMap.unregisterAll(*plugin.registeredCommands.toTypedArray())
		plugin.registeredCommands.clear()

		plugins.remove(plugin)
	}

	fun loadPlugins() {
		val folder = File(loritta.instanceConfig.loritta.folders.plugins)

		for (file in folder.listFiles().filter { it.extension == "jar" }) {
			loadPlugin(file)
		}
	}

	fun loadPlugin(file: File) {
		try {
			val info = getPluginInfo(file)

			logger.info("Loading ${info.pluginName} from file...")

			val url = file.toURI().toURL()

			val classLoader = URLClassLoader.newInstance(arrayOf<URL>(url))
			val method = URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)
			method.isAccessible = true
			method.invoke(classLoader, url)

			val clazz = Class.forName(info.main, true, classLoader)

			val plugin = clazz.getConstructor(String::class.java, LorittaBot::class.java).newInstance(info.pluginName, loritta) as LorittaPlugin
			// plugin.loritta = loritta
			// plugin.name = info.pluginName
			// plugin.classLoader = classLoader
			// plugin.pluginFile = file

			loadPlugin(plugin)
		} catch (e: Throwable) {
			logger.error(e) { "Exception while loading plugin $file" }
		}
	}

	fun getPluginInfo(file: File): PluginDescription {
		val jar = JarFile(file)
		val entry = jar.getJarEntry("plugin.yml") ?: throw InvalidPluginException("Jar does not contain plugin.yml")

		val stream = jar.getInputStream(entry)

		val result = stream.bufferedReader().readText()

		stream.close()
		jar.close()

		return Constants.MAPPER.readValue(result)
	}

	class PluginDescription @JsonCreator constructor(
			@JsonProperty("name")
			val pluginName: String,
			@JsonProperty("main")
			val main: String
	)
}