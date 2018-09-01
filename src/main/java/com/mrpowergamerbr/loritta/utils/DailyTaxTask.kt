package com.mrpowergamerbr.loritta.utils

import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOneModel
import com.mongodb.client.model.Updates
import com.mongodb.client.model.WriteModel
import com.mrpowergamerbr.loritta.Loritta
import mu.KotlinLogging
import org.bson.Document
import java.io.File
import java.util.*

class DailyTaxTask : Runnable {
	companion object {
		private val logger = KotlinLogging.logger {}
		const val MARRIAGE_DAILY_TAX = 100
		var alreadySentDMs = false
	}

	override fun run() {
		runDailyTax(false)
	}

	fun runDailyTax(force: Boolean) {
		val lastDailyTax = File(Loritta.FOLDER, "last_daily_tax")

		val hour = Calendar.getInstance()[Calendar.HOUR_OF_DAY]

		if (!force) {
			if (18 != hour && 19 != hour)
				return

			if (lastDailyTax.exists()) {
				val lastWas = lastDailyTax.readText().toLong()

				if (3_600_000 + lastWas > System.currentTimeMillis()) {
					return
				}
			}
		}

		try {
			if (hour == 18) {
				logger.info("Avisando sobre a taxa diária!")

				val documents = loritta.usersColl.find(
						Filters.and(
								Filters.exists("marriedWith"),
								Filters.lt("dreams", MARRIAGE_DAILY_TAX)
						)
				).toMutableList()

				for (document in documents) {
					val user = lorittaShards.getUserById(document.userId) ?: continue

					try {
						user.openPrivateChannel().queue {
							it.sendMessage("Atenção! Você precisa ter no mínimo 100 Sonhos até as 19:00 de hoje para você continuar o seu casamento! Casamentos custam caro, e você precisa ter no mínimo 100 Sonhos todos os dias para conseguir manter ele!").queue()
						}
					} catch (e: Exception) {}
				}
				alreadySentDMs = true
				return
			}
			alreadySentDMs = false

			logger.info("Executando a taxa diária!")

			// MARRY
			val documents = loritta.usersColl.find(
					Filters.and(
							Filters.exists("marriedWith"),
							Filters.lt("dreams", MARRIAGE_DAILY_TAX)
					)
			).toMutableList()

			loritta.usersColl.updateMany(
					Filters.and(
							Filters.exists("marriedWith"),
							Filters.gte("dreams", MARRIAGE_DAILY_TAX)
					),
					Updates.inc("dreams", -MARRIAGE_DAILY_TAX)
			)

			// Okay, tudo certo, vamos lá!
			val bulk = mutableListOf<WriteModel<Document>>()

			for (document in documents) {
				val marriedWith = lorittaShards.getUserById(document.marriedWith)
				val user = lorittaShards.getUserById(document.userId)

				if (user != null) {
					try {
						user.openPrivateChannel().queue {
							it.sendMessage("Você não teve dinheiro suficiente para manter o casamento... Infelizmente você foi divorciado...").queue()
						}
					} catch (e: Exception) {}
				}

				if (marriedWith != null) {
					try {
						marriedWith.openPrivateChannel().queue {
							it.sendMessage("Seu parceiro não teve dinheiro suficiente para manter o casamento... Infelizmente você foi divorciado...").queue()
						}
					} catch (e: Exception) {}
				}

				bulk.add(
						UpdateOneModel<Document>(
								Filters.eq("_id", document.userId),
								Updates.unset("marriedWith")
						)
				)
				bulk.add(
						UpdateOneModel<Document>(
								Filters.eq("_id", document.marriedWith),
								Updates.unset("marriedWith")
						)
				)
			}

			if (bulk.isNotEmpty()) {
				loritta.mongo.getDatabase(Loritta.config.databaseName).getCollection("users").bulkWrite(
						bulk
				)
			}

			lastDailyTax.writeText(
					System.currentTimeMillis().toString()
			)
		} catch (e: Exception) {
			logger.error("Erro ao atualizar a taxa diária!", e)
		}
	}
}