package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.dao.Marriage
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.DonationKeys
import com.mrpowergamerbr.loritta.tables.Profiles
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.dao.Payment
import net.perfectdreams.loritta.tables.Payments
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.payments.PaymentReason
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class DailyTaxTask : Runnable {
	companion object {
		private val logger = KotlinLogging.logger {}
		const val MARRIAGE_DAILY_TAX = 100L
		var alreadySentDMs = false
	}

	override fun run() {
		runDailyTax(false)
	}

	fun runDailyTax(force: Boolean) {
		if (!loritta.isMaster)
			return

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

				// Dar aquela reaproveitada já que aqui já estamos avisando coisas a cada X horas do dia na moralzinha
				val alreadyPaymentNotifiedUsers = mutableListOf<Long>()

				// Avisar sobre keys expirando
				val soonToBeExpiredMatchingKeys = transaction(Databases.loritta) {
					val soonToBeExpiredKeys = DonationKey.find {
						DonationKeys.expiresAt lessEq (System.currentTimeMillis() + 259_200_000) and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) // 3 dias
					}.toMutableList()
					val soonToBeExpiredMatchingKeys = mutableListOf<Pair<DonationKey, Long>>()

					for (key in soonToBeExpiredKeys) {
						val serverUsingTheKey = key.activeIn
						if (serverUsingTheKey != null) {
							soonToBeExpiredMatchingKeys.add(Pair(key, serverUsingTheKey.guildId))
						}
					}

					return@transaction soonToBeExpiredMatchingKeys
				}

				for ((donationKey, guildId) in soonToBeExpiredMatchingKeys) {
					val user = lorittaShards.shardManager.retrieveUserById(donationKey.userId).complete() ?: continue // Ignorar caso o usuário não exista
					val guild = lorittaShards.getGuildById(guildId) ?: continue // Apenas avise caso a key esteja sendo usada em algum servidor

					val dayNow = TimeUnit.MILLISECONDS.toDays(donationKey.expiresAt - System.currentTimeMillis())

					logger.info { "Avisando para $user que a key $donationKey de $guild irá expirar em breve! (Falta ${dayNow} dias para expirar)" }

					val embed = EmbedBuilder()
							.setTitle("\uD83D\uDD11 Sua key está quase expirando!")
							.setColor(Constants.LORITTA_AQUA)

					val coolFeatures by lazy {
						val strBuilder = StringBuilder()
						strBuilder.append("${Emotes.LORI_WOW} A sensação incrível de ter me ajudado a ficar online!")
						strBuilder.append("\n")
						strBuilder.append("${Emotes.LORI_WOW} Deixar que DJs no seu servidor possam usar `+volume`!")
						strBuilder.append("\n")
						strBuilder.append("${Emotes.LORI_WOW} Uma badge exclusiva para membros do seu servidor!")
						strBuilder.append("\n")
						if (59.99 >= donationKey.value) {
							strBuilder.append("${Emotes.LORI_WOW} Multiplicador de sonhos para os membros ativos do seu servidor!")
						}
						strBuilder.toString()
					}

					when (dayNow) {
						2L -> {
							embed.setThumbnail("https://i.imgur.com/HSmy9yK.png")
							embed.setDescription("Estou passando para avisar que a key de R$ ${donationKey.value} que você está usando em `${guild.name}` irá expirar em breve!\n\nSe você quiser manter a key, renove ela [no meu website](${loritta.instanceConfig.loritta.website.url}donate) antes dela expirar para conseguir 20% de desconto! ${Emotes.LORI_HAPPY}\n\nContinue sendo incrível! ${Emotes.LORI_OWO}")
						}
						1L -> {
							embed.setThumbnail("https://i.imgur.com/oleb4HP.png")
							embed.setDescription("Hey! Estou passando para avisar que a key de R$ ${donationKey.value} que você está usando em `${guild.name}` irá expirar em breve!\n\nSe a sua key expirar, você irá perder vantagens incríveis, como...\n$coolFeatures\n\nAh, e não se esqueça das vantagens que você ganha como usuário!\n\nSe você quiser manter a key, renove ela [no meu website](${loritta.instanceConfig.loritta.website.url}donate) antes dela expirar para conseguir 20% de desconto! ${Emotes.LORI_HAPPY}\n\nMas se você não puder, tudo bem... continue sendo incrível! ${Emotes.LORI_OWO}")
						}
						0L -> {
							embed.setThumbnail("https://i.imgur.com/L8oyJQ7.png")
							embed.setDescription("Heeeeey! Estou passando para avisar que a key de R$ ${donationKey.value} que você está usando em `${guild.name}` irá expirar em breve!\n\nSério mesmo que você vai deixar a sua key expirar e deixar de ter vantagens incríveis como...\n$coolFeatures\nE ainda perder todas as vantagens que você ganha como usuário?\n\nMas eu vou te dar uma promoção exclusiva: Renove a key e eu irei te dar 20% de desconto! Pense bem, se você não renovar e quiser as vantagens de novo terá que pagar o preço sem desconto! ${Emotes.LORI_CRYING}\n\nSe você quiser renovar a key, [aqui está o link](${loritta.instanceConfig.loritta.website.url}donate)! ${Emotes.LORI_OWO}\n\nMas obrigada por ter contribuido! Sua ajuda me ajudou bastante a continuar a ficar online, divertir e ajudar outros membros, você é uma pessoa incrível e, por favor, continue sendo uma pessoa assim! ${Emotes.LORI_HUG}\n\nObrigada por tudo... e continue sendo uma pessoa incrível! ${Emotes.LORI_OWO}")
						}
					}

					user.openPrivateChannel().queue {
						it.sendMessage(embed.build()).queue()
					}

					alreadyPaymentNotifiedUsers.add(user.idLong)
				}

				// Avisar para pagar a doação novamente antes que ela "expire"
				val soonToBeExpiredDonations = transaction(Databases.loritta) {
					val soonToBeExpiredDonations = mutableListOf<Payment>()

					val possiblySoonToBeExpiredDonations = Payment.find {
						Payments.expiresAt lessEq (System.currentTimeMillis() + 259_200_000) and (Payments.expiresAt greaterEq System.currentTimeMillis() and (Payments.reason eq PaymentReason.DONATION)) // 3 dias
					}

					// O soonToBeExpiredDonations tem os pagamentos que "vão" expirar logo... mas tem uma coisa!
					// E se o cara doar? Precisamos verificar se o cara já não pagou de novo!
					for (soonToBeExpiredDonation in possiblySoonToBeExpiredDonations) {
						val newDonationsCount = Payment.find {
							Payments.paidAt greater (soonToBeExpiredDonation.paidAt ?: Long.MAX_VALUE) and (Payments.reason eq PaymentReason.DONATION) // greater, pois não queremos pegar o mesmo pagamento!
						}.count()

						if (newDonationsCount == 0L && !alreadyPaymentNotifiedUsers.contains(soonToBeExpiredDonation.userId)) {
							soonToBeExpiredDonations.add(soonToBeExpiredDonation)
						}
					}

					return@transaction soonToBeExpiredDonations
				}

				// Hora de avisar aos usuários que a doação deles irá acabar!
				for ((index, soonToBeExpiredDonation) in soonToBeExpiredDonations.distinctBy { it.userId }.withIndex()) {
					val user = lorittaShards.shardManager.retrieveUserById(soonToBeExpiredDonation.userId).complete() ?: continue // Ignorar caso o usuário não exista

					val embed = EmbedBuilder()
							.setTitle("\uD83D\uDCB8 Faz bastante tempo que você não doa...")
							.setColor(Constants.LORITTA_AQUA)

					val dayNow = TimeUnit.MILLISECONDS.toDays((soonToBeExpiredDonation.expiresAt ?: Long.MAX_VALUE) - System.currentTimeMillis())

					logger.info { "Avisando para $user que a doação $soonToBeExpiredDonation irá expirar em breve! (Falta ${dayNow} dias para expirar)" }

					when (dayNow) {
						2L -> {
							embed.setThumbnail("https://i.imgur.com/HSmy9yK.png")
							embed.setDescription("Estou passando para avisar que irá fazer 30 dias desde a sua última contribuição! ${Emotes.LORI_HAPPY}\n\nSe você quiser continuar a ser um maravilhoso contribuidor que me ajuda a pagar o meu aluguel e a ficar online, por favor, doe novamente [no meu website](${loritta.instanceConfig.loritta.website.url}donate)! ${Emotes.LORI_TEMMIE}\n\nContinue sendo incrível! ${Emotes.LORI_OWO}")
						}
						1L -> {
							embed.setThumbnail("https://i.imgur.com/oleb4HP.png")
							embed.setDescription("Hey! Estou passando para avisar que irá fazer 30 dias desde a sua última contribuição!\n\nEu sei que é chatinho ficar pedindo para doar novamente, mas se você puder... por favoooor doeeeee! ${Emotes.LORI_CRYING}\n\nMesmo que a sua doação seja pequena, ela sempre me ajuda a ficar mais tempo online!\n\nMas bem, se você estiver afim de doar novamente... Aqui está o [link para doar](${loritta.instanceConfig.loritta.website.url}donate)! ${Emotes.LORI_HAPPY}\n\nMas se você não puder, tudo bem... continue sendo incrível! ${Emotes.LORI_OWO}")
						}
						0L -> {
							embed.setThumbnail("https://i.imgur.com/L8oyJQ7.png")
							embed.setDescription("Heeeeey! Estou passando para avisar que irá fazer 30 dias desde a sua última contribuição!\n\nEssa será a última vez que eu irei pedir para você contribuir novamente (eu sei, por dentro você deve estar \"nossa, finalmente hein lori, já estava ficando chato ${Emotes.LORI_SHRUG}\")...\n\nMas obrigada por ter contribuido! Sua ajuda me ajudou bastante a continuar a ficar online, divertir e ajudar outros membros, você é uma pessoa incrível e, por favor, continue sendo uma pessoa assim! ${Emotes.LORI_HUG}\n\nSe você estiver afim de doar novamente, [aqui está o link](${loritta.instanceConfig.loritta.website.url}donate)! ${Emotes.LORI_OWO}\n\nObrigada por tudo... e continue sendo uma pessoa incrível! ${Emotes.LORI_OWO}")
						}
					}

					user.openPrivateChannel().queueAfter(index.toLong(), TimeUnit.SECONDS) {
						it.sendMessage(embed.build()).queue()
					}
				}

				// MARRY - Aviar sobre sonhos
				val documents = transaction(Databases.loritta) {
					Profile.find { Profiles.marriage.isNotNull() and Profiles.money.less(MARRIAGE_DAILY_TAX) }.toMutableList()
				}

				for ((index, document) in documents.withIndex()) {
					val user = lorittaShards.shardManager.retrieveUserById(document.userId.toString()).complete() ?: continue

					try {
						user.openPrivateChannel().queueAfter(index.toLong(), TimeUnit.SECONDS) {
							it.sendMessage("Atenção! Você precisa ter no mínimo 100 Sonhos até as 19:00 de hoje para você continuar o seu casamento! Casamentos custam caro, e você precisa ter no mínimo 100 Sonhos todos os dias para conseguir manter ele!")
									.queue()
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

				// Primeiro iremos pegar todos os casamentos que serão deletados ANTES de retirar os sonhos
				val usersThatShouldHaveTheirMarriageRemoved = transaction(Databases.loritta) {
					Profile.find {
						Profiles.marriage.isNotNull() and Profiles.money.less(MARRIAGE_DAILY_TAX)
					}.toMutableList()
				}

				// MARRY - Remover sonhos de quem merece
				usersThatShouldHaveTheirMarriageRemoved.forEach {
					transaction(Databases.loritta) {
						Profiles.update({ Profiles.id eq it.id }) {
							with(SqlExpressionBuilder) {
								it.update(money, money - MARRIAGE_DAILY_TAX)
							}
						}
					}
				}

				val removeMarriages = mutableListOf<Marriage>()

				// Okay, tudo certo, vamos lá!
				for ((index, document) in usersThatShouldHaveTheirMarriageRemoved.withIndex()) {
					val marriage = transaction(Databases.loritta) { document.marriage } ?: continue

					removeMarriages.add(marriage)

					val marriedWithId = if (marriage.user1 == document.userId) {
						marriage.user2
					} else {
						marriage.user1
					}.toString()

					val marriedWith = lorittaShards.shardManager.retrieveUserById(marriedWithId).complete()
					val user = lorittaShards.shardManager.retrieveUserById(document.userId.toString()).complete()

					// The "queueAfter" is to avoid too many requests at the same time
					if (user != null) {
						try {
							user.openPrivateChannel().queueAfter(index.toLong(), TimeUnit.SECONDS) {
								it.sendMessage("Você não teve dinheiro suficiente para manter o casamento... Infelizmente você foi divorciado...")
										.queue()
							}
						} catch (e: Exception) {
						}
					}

					if (marriedWith != null) {
						try {
							marriedWith.openPrivateChannel().queueAfter(index.toLong(), TimeUnit.SECONDS) {
								it.sendMessage("Seu parceiro não teve dinheiro suficiente para manter o casamento... Infelizmente você foi divorciado...")
										.queue()
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