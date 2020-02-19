package net.perfectdreams.loritta.website.routes.api.v1.economy

import com.github.salomonbrys.kotson.*
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.jsonParser
import io.ktor.application.ApplicationCall
import io.ktor.request.receiveText
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.SonhosTransaction
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class PostTransferBalanceExternalRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/economy/transfer/garticos") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val body = call.receiveText()
		val json = jsonParser.parse(body)
		val receiverId = json["receiverId"].string
		val garticos = json["garticos"].long
		val transferRate = json["transferRate"].double

		val profile = com.mrpowergamerbr.loritta.utils.loritta.getOrCreateLorittaProfile(receiverId)

		logger.info { "$receiverId (has ${profile.money} dreams) is transferring $garticos garticos to Loritta with transfer rate is $transferRate" }
		val finalMoney = (garticos * transferRate)

		transaction(Databases.loritta) {
			profile.money += finalMoney

			SonhosTransaction.insert {
				it[givenBy] = null
				it[receivedBy] = receiverId.toLong()
				it[givenAt] = System.currentTimeMillis()
				it[quantity] = finalMoney.toBigDecimal()
				it[reason] = SonhosPaymentReason.GARTICOS_TRANSFER
			}
		}

		logger.info { "$receiverId (now has ${profile.money} dreams) transferred $garticos garticos to Loritta with transfer rate is $transferRate" }

		call.respondJson(
				jsonObject(
						"balance" to profile.money
				)
		)
	}
}