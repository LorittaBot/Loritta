package com.mrpowergamerbr.loritta.utils.networkbans

import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

class ApplyBansTask : Runnable {
	companion object {
		private val logger = KotlinLogging.logger {}
		val banWaveUsers = ConcurrentHashMap<Long, String>()
	}

	override fun run() {
		try {
			logger.info { "Applying user ban wave..." }

			/* banWaveUsers.forEach {
				logger.info { "Banning ${it.key} due to ${it.value} " }

				val profile = loritta.getLorittaProfile(it.key)
				if (profile != null) {
					transaction(Databases.loritta) {
						profile.isBanned = true
						profile.bannedReason = it.value
					}
				}
			} */

			banWaveUsers.clear()
		} catch (e: Exception) {
			logger.error(e) { "Erro ao processar usuários banidos na network!" }
		}
	}
}