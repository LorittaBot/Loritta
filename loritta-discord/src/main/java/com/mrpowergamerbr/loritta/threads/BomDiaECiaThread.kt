package com.mrpowergamerbr.loritta.threads

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import com.mrpowergamerbr.loritta.utils.loritta
import mu.KotlinLogging

/**
 * Thread para o Bom Dia & Cia
 */
class BomDiaECiaThread : Thread("Bom Dia & Cia") {
	private val logger = KotlinLogging.logger {}

	override fun run() {
		super.run()

		while (true) {
			try {
				handleAnnouncing()
			} catch (e: Exception) {
				e.printStackTrace()
			}
			sleep(1000)
		}
	}

	fun handleAnnouncing() {
		// Gerar um tempo aleatório entre 15 minutos e 30 minutos
		val wait = Loritta.RANDOM.nextLong(900_000, 2_700_000)
		val estimatedTime = wait + System.currentTimeMillis()
		logger.info("Iremos esperar ${wait} até o próximo Funk do Yudi ${estimatedTime.humanize(loritta.localeManager.getLocaleById("default"))}")
		sleep(wait)
		loritta.bomDiaECia.handleBomDiaECia(false)
	}
}