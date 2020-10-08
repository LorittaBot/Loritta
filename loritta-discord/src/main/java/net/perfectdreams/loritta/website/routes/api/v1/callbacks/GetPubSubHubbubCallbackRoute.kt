package net.perfectdreams.loritta.website.routes.api.v1.callbacks

import net.perfectdreams.loritta.website.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.BaseRoute

class GetPubSubHubbubCallbackRoute(loritta: LorittaDiscord) : BaseRoute(loritta, "/api/v1/callbacks/pubsubhubbub") {
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