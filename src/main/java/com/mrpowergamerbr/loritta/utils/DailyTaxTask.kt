package com.mrpowergamerbr.loritta.utils

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mrpowergamerbr.loritta.Loritta
import mu.KotlinLogging
import java.io.File
import java.util.*

class DailyTaxTask : Runnable {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun run() {
		runDailyTax(false)
	}

	fun runDailyTax(force: Boolean) {
		val lastDailyTax = File(Loritta.FOLDER, "last_daily_tax")

		if (!force) {
			val calendar = Calendar.getInstance()

			if (19 == calendar[Calendar.HOUR_OF_DAY])
				return

			if (lastDailyTax.exists()) {
				val lastWas = lastDailyTax.readText().toLong()

				if (System.currentTimeMillis() > 3_600_000 + lastWas) {
					return
				}
			}
		}

		try {
			logger.info("Executando a taxa diária!")

			// MARRY
			loritta.usersColl.updateMany(
					Filters.and(
							Filters.exists("marriedWith"),
							Filters.gte("dreams", 200)
					),
					Updates.inc("dreams", -200)
			)

			loritta.usersColl.updateMany(
					Filters.and(
							Filters.exists("marriedWith"),
							Filters.lt("dreams", 200)
					),
					Updates.unset("marriedWith")
			)

			lastDailyTax.writeText(
					System.currentTimeMillis().toString()
			)
		} catch (e: Exception) {
			logger.error("Erro ao atualizar a taxa diária!")
		}
	}
}