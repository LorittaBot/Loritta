package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.economy

import com.github.salomonbrys.kotson.*
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.Body
import org.jooby.mvc.POST
import org.jooby.mvc.Path

@Path("/api/v1/economy/transfer/garticos")
class TransferBalanceExternalController {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	@POST
	@LoriRequiresAuth(LoriAuthLevel.API_KEY)
	@LoriDoNotLocaleRedirect(true)
	fun handle(req: Request, res: Response, @Body body: String) {
		res.type(MediaType.json)

		val json = jsonParser.parse(body)
		val receiverId = json["receiverId"].string
		val garticos = json["garticos"].long
		val transferRate = json["transferRate"].double

		val profile = loritta.getOrCreateLorittaProfile(receiverId)

		logger.info { "$receiverId (has ${profile.money} dreams) is transferring $garticos garticos to Loritta with transfer rate is $transferRate" }
		transaction(Databases.loritta) {
			profile.money += (garticos * transferRate)
		}
		logger.info { "$receiverId (now has ${profile.money} dreams) transferred $garticos garticos to Loritta with transfer rate is $transferRate" }

		res.send(
				jsonObject(
						"balance" to profile.money
				)
		)
	}
}