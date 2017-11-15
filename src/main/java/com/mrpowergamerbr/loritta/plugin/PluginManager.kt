package com.mrpowergamerbr.loritta.plugin

import com.github.salomonbrys.kotson.fromJson
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.gson
import com.mrpowergamerbr.loritta.commands.CommandBase
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.net.URL
import java.net.URLClassLoader
import java.util.jar.JarFile
import java.util.stream.Collectors


class PluginManager {
	val plugins = mutableListOf<LorittaPlugin>()

	fun clearPlugins() {
		plugins.forEach { this.unloadPlugin(it) }
		plugins.clear()
	}

	fun unloadPlugin(plugin: LorittaPlugin) {
		plugin.onDisable()
	}

	fun loadPlugins() {
		val folder = File(Loritta.config.pluginFolder)

		for (file in folder.listFiles()) {
			loadPlugin(file)
		}
	}

	fun loadPlugin(file: File) {
		val info = getPluginInfo(file)

		println("Loading ${info.pluginName}...")

		val url = file.toURI().toURL()

		val classLoader = URLClassLoader.newInstance(arrayOf<URL>(url))
		val method = URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)
		method.isAccessible = true
		method.invoke(classLoader, url)

		val clazz = Class.forName(info.main, true, classLoader)

		val plugin = clazz.newInstance() as LorittaPlugin
		plugin.name = info.pluginName
		plugin.classLoader = classLoader
		plugins.add(plugin)
		plugin.onEnable()

		println("${info.pluginName} loaded successfully!")
	}

	fun getPluginInfo(file: File): PluginDescription {
		val jar = JarFile(file)
		val entry = jar.getJarEntry("plugin.json") ?: throw RuntimeException(FileNotFoundException("Jar does not contain plugin.json"))

		val stream = jar.getInputStream(entry);

		val result = BufferedReader(InputStreamReader(stream))
				.lines().collect(Collectors.joining("\n"))

		stream.close()
		jar.close()

		return gson.fromJson(result)
	}

	fun getExternalCommands(): Collection<CommandBase> {
		val commands = mutableListOf<CommandBase>()

		for (plugin in plugins) {
			commands.addAll(plugin.getCommands())
		}

		return commands
	}

	class PluginDescription(val pluginName: String, val main: String)
}