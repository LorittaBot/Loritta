package net.perfectdreams.loritta.website.routes.api.v1.callbacks

import io.ktor.application.*
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.utils.extensions.hostFromHeader
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class CreateWebhookRoute(val loritta: LorittaDiscord) : BaseRoute("/api/v1/callbacks/discord-webhook") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onRequest(call: ApplicationCall) {
		val hostHeader = call.request.hostFromHeader()
		val code = call.parameters["code"]

		val auth = TemmieDiscordAuth(
				com.mrpowergamerbr.loritta.utils.loritta.discordConfig.discord.clientId,
				com.mrpowergamerbr.loritta.utils.loritta.discordConfig.discord.clientSecret,
				code,
				"https://$hostHeader/api/v1/callbacks/discord-webhook",
				listOf("webhook.incoming")
		)

		val authExchangePayload = auth.doTokenExchange()
		call.respondJson(authExchangePayload["webhook"])
	}
}