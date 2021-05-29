package net.perfectdreams.loritta.website.routes.api.v1.loritta

import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.commands.vanilla.economy.LoraffleCommand
import com.mrpowergamerbr.loritta.threads.RaffleThread
import io.ktor.application.*
import io.ktor.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson

class PostRaffleStatusRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/loritta/raffle") {
	companion object {
		val logger = KotlinLogging.logger {}
		val mutex = Mutex()
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		RaffleThread.buyingOrGivingRewardsMutex.withLock {
			val json = withContext(Dispatchers.IO) { JsonParser.parseString(call.receiveText()).obj }

			val userId = json["userId"].string
			val quantity = json["quantity"].int
			val localeId = json["localeId"].string

			val currentUserTicketQuantity = RaffleThread.userIds.count { it.first == userId }

			if (RaffleThread.userIds.count { it.first == userId } + quantity > LoraffleCommand.MAX_TICKETS_BY_USER_PER_ROUND) {
				if (currentUserTicketQuantity == LoraffleCommand.MAX_TICKETS_BY_USER_PER_ROUND) {
					call.respondJson(
						jsonObject(
							"status" to LoraffleCommand.BuyRaffleTicketStatus.THRESHOLD_EXCEEDED.toString()
						)
					)
				} else {
					call.respondJson(
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

			mutex.withLock {
				val lorittaProfile = com.mrpowergamerbr.loritta.utils.loritta.getOrCreateLorittaProfile(userId)

				if (lorittaProfile.money >= requiredCount) {
					loritta.newSuspendedTransaction {
						lorittaProfile.takeSonhosAndAddToTransactionLogNested(
							requiredCount,
							SonhosPaymentReason.RAFFLE
						)
					}

					for (i in 0 until quantity) {
						RaffleThread.userIds.add(Pair(userId, localeId))
					}

					RaffleThread.logger.info("${userId} comprou $quantity tickets por ${requiredCount}! (Antes ele possuia ${lorittaProfile.money + requiredCount}) sonhos!")

					com.mrpowergamerbr.loritta.utils.loritta.raffleThread.save()

					call.respondJson(
						jsonObject(
							"status" to LoraffleCommand.BuyRaffleTicketStatus.SUCCESS.toString()
						)
					)
				} else {
					call.respondJson(
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