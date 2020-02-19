package net.perfectdreams.loritta.website.routes.api.v1.loritta

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.mrpowergamerbr.loritta.commands.vanilla.economy.PagarCommand
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.LorittaPrices
import com.mrpowergamerbr.loritta.utils.jsonParser
import io.ktor.application.ApplicationCall
import io.ktor.request.receiveText
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.SonhosTransaction
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class PostTransferBalanceRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/loritta/transfer-balance") {
	companion object {
		private val mutex = Mutex()
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val json = jsonParser.parse(call.receiveText())
		val giverId = json["giverId"].long
		val receiverId = json["receiverId"].long
		val howMuch = json["howMuch"].double

		logger.info { "Initializing transaction between $giverId and $receiverId, $howMuch sonhos will be transferred. Is mutex locked? ${mutex.isLocked}" }
		mutex.withLock {
			val receiverProfile = com.mrpowergamerbr.loritta.utils.loritta.getOrCreateLorittaProfile(receiverId)
			val giverProfile = com.mrpowergamerbr.loritta.utils.loritta.getOrCreateLorittaProfile(giverId)

			if (receiverProfile.money.isNaN() || giverProfile.money.isNaN()) {
				call.respondJson(
						jsonObject(
								"status" to PagarCommand.PayStatus.INVALID_MONEY_STATUS.toString()
						)
				)
				return@withLock
			}

			if (howMuch > giverProfile.money) {
				call.respondJson(
						jsonObject(
								"status" to PagarCommand.PayStatus.NOT_ENOUGH_MONEY.toString()
						)
				)
				return@withLock
			}

			val beforeGiver = giverProfile.money
			val beforeReceiver = receiverProfile.money

			val activeMoneyFromDonations = com.mrpowergamerbr.loritta.utils.loritta.getActiveMoneyFromDonations(giverId)
			val taxBypass = activeMoneyFromDonations >= LorittaPrices.NO_PAY_TAX

			val taxedMoney = if (taxBypass) { 0.0 } else { Math.ceil(PagarCommand.TRANSACTION_TAX * howMuch.toDouble()) }
			val finalMoney = howMuch - taxedMoney

			transaction(Databases.loritta) {
				giverProfile.money -= howMuch
				receiverProfile.money += finalMoney

				if (taxedMoney != 0.0) {
					SonhosTransaction.insert {
						it[givenBy] = giverProfile.id.value
						it[receivedBy] = null
						it[givenAt] = System.currentTimeMillis()
						it[quantity] = taxedMoney.toBigDecimal()
						it[reason] = SonhosPaymentReason.PAYMENT_TAX
					}
				}

				SonhosTransaction.insert {
					it[givenBy] = giverProfile.id.value
					it[receivedBy] = receiverProfile.id.value
					it[givenAt] = System.currentTimeMillis()
					it[quantity] = finalMoney.toBigDecimal()
					it[reason] = SonhosPaymentReason.PAYMENT
				}
			}

			logger.info { "$giverId (antes possuia ${beforeGiver} sonhos) transferiu ${howMuch} sonhos para ${receiverProfile.userId} (antes possuia ${beforeReceiver} sonhos, recebeu apenas $finalMoney (taxado!))" }

			call.respondJson(
					jsonObject(
							"status" to PagarCommand.PayStatus.SUCCESS.toString(),
							"finalMoney" to finalMoney
					)
			)
		}
	}
}