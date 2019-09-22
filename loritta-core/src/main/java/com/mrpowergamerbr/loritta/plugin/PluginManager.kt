package com.mrpowergamerbr.loritta.plugin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.readValue
import com.mrpowergamerbr.loritta.utils.Constants
import mu.KotlinLogging
import net.perfectdreams.loritta.api.platform.LorittaBot
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.jar.JarFile

class PluginManager(val loritta: LorittaBot) {
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
		loritta.commandManager.unregisterCommands(*plugin.commands.toTypedArray())
		plugin.commands.clear()
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

			logger.info("Loading ${info.pluginName}...")

			val url = file.toURI().toURL()

			val classLoader = URLClassLoader.newInstance(arrayOf<URL>(url))
			val method = URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)
			method.isAccessible = true
			method.invoke(classLoader, url)

			val clazz = Class.forName(info.main, true, classLoader)

			val plugin = clazz.getConstructor().newInstance() as LorittaPlugin
			plugin.loritta = loritta
			plugin.name = info.pluginName
			plugin.classLoader = classLoader
			plugin.pluginFile = file

			plugin.onEnable()
			plugins.add(plugin)

			logger.info("${info.pluginName} loaded successfully!")
		} catch (e: Exception) {
			logger.error(e) { "Exception while loading plugin $file"}
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