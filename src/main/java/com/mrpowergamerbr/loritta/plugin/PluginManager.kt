package com.mrpowergamerbr.loritta.plugin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.loritta
import mu.KotlinLogging
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.jar.JarFile

class PluginManager {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	val plugins = mutableListOf<LorittaPlugin>()

	fun getPlugin(name: String): LorittaPlugin? {
		return plugins.firstOrNull { it.name == name }
	}

	fun clearPlugins() {
		plugins.forEach { this.unloadPlugin(it) }
		plugins.clear()
	}

	fun unloadPlugin(plugin: LorittaPlugin) {
		plugin.onDisable()
		loritta.lorittaCommandManager.unregisterCommands(*plugin.commands.toTypedArray())
		plugin.commands.clear()
	}

	fun loadPlugins() {
		val folder = File(Loritta.FOLDER, "plugins")

		for (file in folder.listFiles().filter { it.extension == "jar" }) {
			loadPlugin(file)
		}
	}

	fun loadPlugin(file: File) {
		val info = getPluginInfo(file)

		logger.info("Loading ${info.pluginName}...")

		val url = file.toURI().toURL()

		val classLoader = URLClassLoader.newInstance(arrayOf<URL>(url))
		val method = URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)
		method.isAccessible = true
		method.invoke(classLoader, url)

		val clazz = Class.forName(info.main, true, classLoader)

		val plugin = clazz.getConstructor(String::class.java, URLClassLoader::class.java, File::class.java).newInstance(info.pluginName, classLoader, file) as LorittaPlugin
		plugins.add(plugin)
		plugin.onEnable()

		logger.info("${info.pluginName} loaded successfully!")
	}

	fun getPluginInfo(file: File): PluginDescription {
		val jar = JarFile(file)
		val entry = jar.getJarEntry("plugin.yml") ?: throw InvalidPluginException("Jar does not contain plugin.yml")

		val stream = jar.getInputStream(entry)

		val result = stream.bufferedReader().readText()

		stream.close()
		jar.close()

		return Constants.YAML.load<PluginDescription>(result)
	}

	class PluginDescription @JsonCreator constructor(
			@JsonProperty("name")
			val pluginName: String,
			@JsonProperty("main")
			val main: String
	)
}