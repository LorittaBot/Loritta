package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.dao.Marriage
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.DonationKeys
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.tables.ServerConfigs
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.utils.Emotes
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
			if (hour == 18 && !alreadySentDMs) {
				logger.info("Avisando sobre a taxa diária!")

				// Dar aquela reaproveitada aqui na moralzinha
				val soonToBeExpiredMatchingKeys = transaction(Databases.loritta) {
					val soonToBeExpiredKeys = DonationKey.find {
						DonationKeys.expiresAt lessEq (System.currentTimeMillis() + 259_200_000) and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) // 3 dias
					}.toMutableList()
					val soonToBeExpiredMatchingKeys = mutableListOf<Pair<DonationKey, Long>>()

					for (key in soonToBeExpiredKeys) {
						val serverUsingTheKey = ServerConfig.find {
							ServerConfigs.donationKey eq key.id
						}.firstOrNull()

						if (serverUsingTheKey != null) {
							soonToBeExpiredMatchingKeys.add(Pair(key, serverUsingTheKey.guildId))
						}
					}

					return@transaction soonToBeExpiredMatchingKeys
				}

				for ((donationKey, guildId) in soonToBeExpiredMatchingKeys) {
					val user = lorittaShards.getUserById(donationKey.userId) ?: continue // Ignorar caso o usuário não exista
					val guild = lorittaShards.getGuildById(guildId) ?: continue // Apenas avise caso a key esteja sendo usada em algum servidor

					val embed = EmbedBuilder()
							.setTitle("\uD83D\uDC4B Hey!")
							.setDescription("Só estou aqui passando para avisar que a key de R$ ${donationKey.value} que você está usando em `${guild.name}` irá expirar em breve!\n\nSe você quiser manter a key, renove ela [no meu website](${loritta.config.loritta.website.url}donate) antes dela expirar para conseguir 20% de desconto! ${Emotes.LORI_HAPPY}")
							.setThumbnail("https://i.imgur.com/HSmy9yK.png")
							.setColor(Constants.LORITTA_AQUA)

					user.openPrivateChannel().queue {
						it.sendMessage(embed.build()).queue()
					}
				}

				val documents = transaction(Databases.loritta) {
					Profile.find { Profiles.marriage.isNotNull() and Profiles.money.less(MARRIAGE_DAILY_TAX.toDouble()) }.toMutableList()
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

			if (hour == 19) {
				alreadySentDMs = false
				logger.info("Executando a taxa diária!")

				lastDailyTax.writeText(
						System.currentTimeMillis().toString()
				)

				// MARRY - Remover sonhos de quem merece
				transaction(Databases.loritta) {
					Profiles.update({ Profiles.marriage.isNotNull() and Profiles.money.greaterEq(MARRIAGE_DAILY_TAX.toDouble()) }) {
						with(SqlExpressionBuilder) {
							it.update(money, money - MARRIAGE_DAILY_TAX.toDouble())
						}
					}
				}

				val usersThatShouldHaveTheirMarriageRemoved = transaction(Databases.loritta) {
					Profile.find {
						Profiles.marriage.isNotNull() and Profiles.money.less(MARRIAGE_DAILY_TAX.toDouble())
					}.toMutableList()
				}

				val removeMarriages = mutableListOf<Marriage>()

				// Okay, tudo certo, vamos lá!
				for (document in usersThatShouldHaveTheirMarriageRemoved) {
					val marriage = transaction(Databases.loritta) { document.marriage } ?: continue

					removeMarriages.add(marriage)

					val marriedWithId = if (marriage.user1 == document.userId) {
						marriage.user2
					} else {
						marriage.user1
					}.toString()

					val marriedWith = lorittaShards.getUserById(marriedWithId)
					val user = lorittaShards.getUserById(document.userId.toString())

					if (user != null) {
						try {
							user.openPrivateChannel().queue {
								it.sendMessage("Você não teve dinheiro suficiente para manter o casamento... Infelizmente você foi divorciado...").queue()
							}
						} catch (e: Exception) {
						}
					}

					if (marriedWith != null) {
						try {
							marriedWith.openPrivateChannel().queue {
								it.sendMessage("Seu parceiro não teve dinheiro suficiente para manter o casamento... Infelizmente você foi divorciado...").queue()
							}
						} catch (e: Exception) {
						}
					}
				}

				transaction(Databases.loritta) {
					Profiles.update({ Profiles.marriage inList removeMarriages.map { it.id }}) {
						it[marriage] = null
					}

					removeMarriages.forEach {
						it.delete()
					}
				}
			}
		} catch (e: Exception) {
			logger.error("Erro ao atualizar a taxa diária!", e)
		}
	}
}