package net.perfectdreams.loritta.api.plugin

interface PluginManager {
	val plugins: List<LorittaPlugin>

	fun getPlugin(name: String) = plugins.firstOrNull { it.name == name }
	fun loadPlugin(plugin: LorittaPlugin)
	fun unloadPlugin(plugin: LorittaPlugin)
}