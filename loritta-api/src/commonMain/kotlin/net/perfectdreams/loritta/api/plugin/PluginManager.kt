package net.perfectdreams.loritta.api.plugin

import mu.KotlinLogging

interface PluginManager {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	val plugins: List<LorittaPlugin>
}