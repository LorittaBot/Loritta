package net.perfectdreams.loritta.website.routes.api.v1.callbacks

import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import net.perfectdreams.sequins.ktor.BaseRoute

class GetPubSubHubbubCallbackRoute(val loritta: LorittaDiscord) : BaseRoute("/api/v1/callbacks/pubsubhubbub") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onRequest(call: ApplicationCall) {
		val hubChallenge = call.parameters["hub.challenge"]

		logger.trace { "hubChallenge=$hubChallenge" }

		if (hubChallenge == null) {
			logger.error { "Recebi um request para ativar uma subscription, mas o request não possuia o hub.challenge!" }
			throw WebsiteAPIException(
					HttpStatusCode.NotFound,
					WebsiteUtils.createErrorPayload(
							LoriWebCode.FORBIDDEN,
							"Missing hub.challenge"
					)
			)
		}

		// Já que a Twitch não suporta verify tokens, nós apenas iremos ignorar os tokens de verificação
		call.respondText(status = HttpStatusCode.OK) { hubChallenge }
	}
}