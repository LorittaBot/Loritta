package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.loritta

import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.commands.vanilla.economy.LoraffleCommand
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.threads.RaffleThread
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
import org.jooby.mvc.GET
import org.jooby.mvc.POST
import org.jooby.mvc.Path

@Path("/api/v1/loritta/raffle")
class GetRaffleStatusController {
	companion object {
		val logger = KotlinLogging.logger {}
		val mutex = Mutex()
	}

	@GET
	@LoriDoNotLocaleRedirect(true)
	fun handle(req: Request, res: Response) {
		res.type(MediaType.json)

		val jsonObject = jsonObject(
				"lastWinnerId" to RaffleThread.lastWinnerId,
				"lastWinnerPrize" to RaffleThread.lastWinnerPrize,
				"currentTickets" to RaffleThread.userIds.size,
				"usersParticipating" to RaffleThread.userIds.distinctBy { it.first }.size,
				"started" to RaffleThread.started
		)

		res.send(jsonObject)
	}

	@POST
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.API_KEY)
	fun handle(req: Request, res: Response, @Body body: String) {
		res.type(MediaType.json)

		val json = jsonParser.parse(body).obj

		val userId = json["userId"].string
		val quantity = json["quantity"].int
		val localeId = json["localeId"].string

		val currentUserTicketQuantity = RaffleThread.userIds.count { it.first == userId }

		if (RaffleThread.userIds.count { it.first == userId } + quantity > LoraffleCommand.MAX_TICKETS_BY_USER_PER_ROUND) {
			if (currentUserTicketQuantity == LoraffleCommand.MAX_TICKETS_BY_USER_PER_ROUND) {
				res.send(
						jsonObject(
								"status" to LoraffleCommand.BuyRaffleTicketStatus.THRESHOLD_EXCEEDED.toString()
						)
				)
			} else {
				res.send(
						jsonObject(
								"status" to LoraffleCommand.BuyRaffleTicketStatus.TOO_MANY_TICKETS.toString(),
								"ticketCount" to currentUserTicketQuantity
						)
				)
			}
			return
		}

		val requiredCount = quantity.toLong() * 250
		logger.info("$userId irá comprar $quantity tickets por ${requiredCount}!")

		runBlocking {
			mutex.withLock {
				val lorittaProfile = loritta.getOrCreateLorittaProfile(userId)

				if (lorittaProfile.money >= requiredCount) {
					transaction(Databases.loritta) {
						lorittaProfile.money -= requiredCount
					}

					for (i in 0 until quantity) {
						RaffleThread.userIds.add(Pair(userId, localeId))
					}

					RaffleThread.logger.info("${userId} comprou $quantity tickets por ${requiredCount}! (Antes ele possuia ${lorittaProfile.money + requiredCount}) sonhos!")

					loritta.raffleThread.save()

					res.send(
							jsonObject(
									"status" to LoraffleCommand.BuyRaffleTicketStatus.SUCCESS.toString()
							)
					)
				} else {
					res.send(
							jsonObject(
									"status" to LoraffleCommand.BuyRaffleTicketStatus.NOT_ENOUGH_MONEY.toString(),
									"canOnlyPay" to requiredCount - lorittaProfile.money
							)
					)
				}
			}
		}
	}
}