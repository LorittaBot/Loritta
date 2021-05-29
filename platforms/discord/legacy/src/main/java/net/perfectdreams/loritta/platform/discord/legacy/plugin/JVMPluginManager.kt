package net.perfectdreams.loritta.platform.discord.legacy.plugin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.readValue
import com.mrpowergamerbr.loritta.Loritta
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
	val loadedFromFile = mutableMapOf<LorittaPlugin, File>()

	override fun loadPlugin(plugin: LorittaPlugin) {
		logger.info { "Loading ${plugin.name}" }

		if (plugin is com.mrpowergamerbr.loritta.plugin.LorittaPlugin)
			logger.warn { "Plugin ${plugin.name} is a legacy plugin. Legacy plugin support is deprecated and will be removed soon" }

		try {
			plugin.onEnable()
		} catch (e: Exception) {
			logger.error(e) { "Exception while enabling plugin ${plugin.name}" }
			unloadPlugin(plugin)
			return
		}
		plugins.add(plugin)
	}

	override fun unloadPlugin(plugin: LorittaPlugin) {
		logger.info { "Disabling ${plugin.name}" }
		try {
			plugin.pluginTasks.forEach { it.cancel() }
			if (plugin is LorittaDiscordPlugin)
				plugin.removeEventListeners(*plugin.eventListeners.toTypedArray())
			plugin.onDisable()
		} catch (e: Exception) {
			logger.error(e) { "Exception while disabling plugin ${plugin.name}" }
		}

		logger.info { "Unregistering ${plugin.registeredCommands} commands..." }
		loritta.commandMap.unregisterAll(*plugin.registeredCommands.toTypedArray())
		plugin.registeredCommands.clear()
		if (plugin is LorittaDiscordPlugin) {
			plugin.eventListeners.clear()
			plugin.routes.clear()
		}

		plugins.remove(plugin)
		loadedFromFile.remove(plugin)
	}

	fun reloadPlugin(plugin: LorittaPlugin) {
		val file = loadedFromFile[plugin] ?: throw RuntimeException("$plugin does not have an associated file with it! Was it loaded directly via another plugin source code?")

		val currentAvailableRoutes = plugins.filterIsInstance<LorittaDiscordPlugin>().flatMap { it.routes }

		unloadPlugin(plugin)
		loadPlugin(file)

		val newlyAvailableRoutes = plugins.filterIsInstance<LorittaDiscordPlugin>().flatMap { it.routes }

		if (!(currentAvailableRoutes.containsAll(newlyAvailableRoutes) && newlyAvailableRoutes.containsAll(currentAvailableRoutes)) && loritta is Loritta && loritta.newWebsiteThread != null) {
			logger.info { "Plugin ${plugin.name} unregistered routes! Restarting WebServer..." }
			loritta.stopWebServer()
			loritta.startWebServer()
		}
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

			val constructors = clazz.constructors

			val firstMatchingConstructor = constructors
					.firstOrNull { it.parameters.getOrNull(1)?.type == LorittaDiscord::class.java }
					?: constructors
							.firstOrNull { it.parameters.getOrNull(1)?.type == LorittaBot::class.java }
					?: throw IllegalArgumentException("No matching constructors found for plugin ${info.pluginName}! Check if you have a primary construction with the parameters (name: String, loritta: LorittaBot|LorittaDiscord)")

			val plugin = firstMatchingConstructor.newInstance(info.pluginName, loritta) as LorittaPlugin
			loadPlugin(plugin)
			loadedFromFile[plugin] = file
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