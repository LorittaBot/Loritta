package com.mrpowergamerbr.loritta.utils.networkbans

import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import mu.KotlinLogging

class ApplyBansTask : Runnable {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun run() {
		try {
			logger.info("Verificando ${loritta.networkBanManager.networkBannedUsers.size} usuários banidos...")

			for (entry in loritta.networkBanManager.networkBannedUsers) {
				try {
					val user = lorittaShards.getUserById(entry.id) ?: continue

					loritta.networkBanManager.punishUser(user, loritta.networkBanManager.createBanReason(entry, true))
				} catch (e: Exception) {
					logger.error(e) { "Erro ao processar entry de ${entry.id}!" }
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao processar usuários banidos na network!" }
		}
	}
}