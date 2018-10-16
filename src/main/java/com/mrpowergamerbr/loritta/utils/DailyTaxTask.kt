package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Profiles
import mu.KotlinLogging
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
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

				val documents = transaction(Databases.loritta) {
					Profile.find { Profiles.marriedWith.isNotNull() and Profiles.money.less(MARRIAGE_DAILY_TAX.toDouble()) }.toMutableList()
				}

				for (document in documents) {
					val user = lorittaShards.getUserById(document.userId.toString()) ?: continue

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
			val documents = transaction(Databases.loritta) {
				val selected = Profile.find { Profiles.marriedWith.isNotNull() and Profiles.money.less(MARRIAGE_DAILY_TAX.toDouble()) }.toMutableList()

				Profiles.update({ Profiles.marriedWith.isNotNull() and Profiles.money.greaterEq(MARRIAGE_DAILY_TAX.toDouble())}) {
					with(SqlExpressionBuilder) {
						it.update(Profiles.money, Profiles.money - MARRIAGE_DAILY_TAX.toDouble())
					}
				}

				selected
			}

			// Okay, tudo certo, vamos lá!
			for (document in documents) {
				if (document.marriedWith == null)
					continue

				val marriedWith = lorittaShards.getUserById(document.marriedWith?.toString())
				val user = lorittaShards.getUserById(document.userId.toString())

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

				transaction(Databases.loritta) {
					Profiles.update({ Profiles.id eq document.userId }) {
						it[Profiles.marriedWith] = null as Long?
					}
					Profiles.update({ Profiles.id eq document.marriedWith }) {
						it[Profiles.marriedWith] = null as Long?
					}
				}
			}

			lastDailyTax.writeText(
					System.currentTimeMillis().toString()
			)
		} catch (e: Exception) {
			logger.error("Erro ao atualizar a taxa diária!", e)
		}
	}
}