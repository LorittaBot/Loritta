package com.mrpowergamerbr.loritta.utils

import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

class UpdateFanArtsTask : Runnable {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun run() {
		if (!loritta.isMaster) {
			try {
				val content = runBlocking { lorittaShards.queryMasterLorittaCluster("/api/v1/loritta/fan-arts").await() }
				loritta.fanArtArtists = Constants.JSON_MAPPER.readValue(gson.toJson(content)) // Gambiarra para converter de Gson para Jackson
			} catch (e: Exception) {
				logger.warn(e) { "Error while trying to update fan arts from master cluster" }
			}
		}
	}
}