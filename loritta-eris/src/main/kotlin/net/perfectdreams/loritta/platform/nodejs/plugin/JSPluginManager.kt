package net.perfectdreams.loritta.platform.nodejs.plugin

import net.perfectdreams.loritta.api.plugin.LorittaPlugin
import net.perfectdreams.loritta.api.plugin.PluginManager
import net.perfectdreams.loritta.platform.nodejs.LorittaNodeJS

class JSPluginManager(val loritta: LorittaNodeJS) : PluginManager {
	override val plugins = mutableListOf<LorittaPlugin>()

	override fun loadPlugin(plugin: LorittaPlugin) {
		console.log("Loading ${plugin.name}")
		try {
			plugin.onEnable()
		} catch (e: Exception) {
			console.log(e)
			unloadPlugin(plugin)
		}
	}

	override fun unloadPlugin(plugin: LorittaPlugin) {
		console.log("Disabling ${plugin.name}")
		try {
			plugin.onDisable()
		} catch (e: Exception) {
			console.log(e)
		}

		loritta.commandMap.unregisterAll(*plugin.registeredCommands.toTypedArray())
		plugin.registeredCommands.clear()

		plugins.remove(plugin)
	}
}