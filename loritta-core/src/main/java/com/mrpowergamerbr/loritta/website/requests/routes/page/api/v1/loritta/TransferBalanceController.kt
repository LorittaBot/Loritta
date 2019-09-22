package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.loritta

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.mrpowergamerbr.loritta.commands.vanilla.economy.PagarCommand
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.LorittaPrices
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.Body
import org.jooby.mvc.POST
import org.jooby.mvc.Path

@Path("/api/v1/loritta/transfer-balance")
class TransferBalanceController {
	companion object {
		private val mutex = Mutex()
		private val logger = KotlinLogging.logger {}
	}

	@POST
	@LoriRequiresAuth(LoriAuthLevel.API_KEY)
	@LoriDoNotLocaleRedirect(true)
	fun handle(req: Request, res: Response, @Body body: String) {
		res.type(MediaType.json)

		val json = jsonParser.parse(body)
		val giverId = json["giverId"].long
		val receiverId = json["receiverId"].long
		val howMuch = json["howMuch"].double

		runBlocking {
			logger.info { "Initializing transaction between $giverId and $receiverId, $howMuch sonhos will be transferred. Is mutex locked? ${mutex.isLocked}" }
			mutex.withLock {
				val receiverProfile = loritta.getOrCreateLorittaProfile(receiverId)
				val giverProfile = loritta.getOrCreateLorittaProfile(giverId)

				if (receiverProfile.money.isNaN() || giverProfile.money.isNaN()) {
					res.send(
							jsonObject(
									"status" to PagarCommand.PayStatus.INVALID_MONEY_STATUS.toString()
							)
					)
					return@withLock
				}

				if (howMuch > giverProfile.money) {
					res.send(
							jsonObject(
									"status" to PagarCommand.PayStatus.NOT_ENOUGH_MONEY.toString()
							)
					)
					return@withLock
				}

				val beforeGiver = giverProfile.money
				val beforeReceiver = receiverProfile.money

				val activeMoneyFromDonations = loritta.getActiveMoneyFromDonations(giverId)
				val taxBypass = activeMoneyFromDonations >= LorittaPrices.NO_PAY_TAX

				val taxedMoney = if (taxBypass) { 0.0 } else { Math.ceil(PagarCommand.TRANSACTION_TAX * howMuch.toDouble()) }
				val finalMoney = howMuch - taxedMoney

				transaction(Databases.loritta) {
					giverProfile.money -= howMuch
					receiverProfile.money += finalMoney
				}

				logger.info { "$giverId (antes possuia ${beforeGiver} sonhos) transferiu ${howMuch} sonhos para ${receiverProfile.userId} (antes possuia ${beforeReceiver} sonhos, recebeu apenas $finalMoney (taxado!))" }

				res.send(
						jsonObject(
								"status" to PagarCommand.PayStatus.SUCCESS.toString(),
								"finalMoney" to finalMoney
						)
				)
			}
		}
	}
}