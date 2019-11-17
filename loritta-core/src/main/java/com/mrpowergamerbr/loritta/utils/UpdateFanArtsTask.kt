package com.mrpowergamerbr.loritta.utils

import com.github.salomonbrys.kotson.fromJson
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

class UpdateFanArtsTask : Runnable {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun run() {
		if (!loritta.isMaster) {
			try {
				loritta.fanArtArtists = gson.fromJson(
						runBlocking { lorittaShards.queryMasterLorittaCluster("/api/v1/loritta/fan-arts").await() }
				)
			} catch (e: Exception) {
				logger.warn(e) { "Error while trying to update fan arts from master cluster" }
			}
		}
	}
}