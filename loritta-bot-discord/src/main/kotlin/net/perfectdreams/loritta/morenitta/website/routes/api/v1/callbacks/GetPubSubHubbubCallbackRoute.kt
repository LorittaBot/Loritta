package net.perfectdreams.loritta.morenitta.website.routes.api.v1.callbacks

import net.perfectdreams.loritta.morenitta.website.LoriWebCode
import net.perfectdreams.loritta.morenitta.website.WebsiteAPIException
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.response.*
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.sequins.ktor.BaseRoute

class GetPubSubHubbubCallbackRoute(val loritta: LorittaBot) : BaseRoute("/api/v1/callbacks/pubsubhubbub") {
	companion object {
		private val logger by HarmonyLoggerFactory.logger {}
	}

	override suspend fun onRequest(call: ApplicationCall) {
		val hubChallenge = call.parameters["hub.challenge"]

		logger.trace { "hubChallenge=$hubChallenge" }

		if (hubChallenge == null) {
			logger.error { "Recebi um request para ativar uma subscription, mas o request não possuia o hub.challenge!" }
			throw WebsiteAPIException(
					HttpStatusCode.NotFound,
					WebsiteUtils.createErrorPayload(
							loritta,
							LoriWebCode.FORBIDDEN,
							"Missing hub.challenge"
					)
			)
		}

		// Já que a Twitch não suporta verify tokens, nós apenas iremos ignorar os tokens de verificação
		call.respondText(status = HttpStatusCode.OK) { hubChallenge }
	}
}