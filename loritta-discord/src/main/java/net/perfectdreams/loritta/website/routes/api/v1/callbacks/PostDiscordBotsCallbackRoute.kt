package net.perfectdreams.loritta.website.routes.api.v1.callbacks

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.request.receiveText
import io.ktor.response.respondText
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.utils.WebsiteVoteSource
import net.perfectdreams.loritta.utils.WebsiteVoteUtils
import net.perfectdreams.loritta.website.routes.BaseRoute

class PostDiscordBotsCallbackRoute(loritta: LorittaDiscord) : BaseRoute(loritta, "/api/v1/callbacks/discord-bots") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onRequest(call: ApplicationCall) {
		val response = call.receiveText()

		logger.info("Recebi payload do Discord Bots!")
		logger.trace { response }

		val authorizationHeader = call.request.header("Authorization")
		if (authorizationHeader == null) {
			logger.error { "Header de Autorização do request não existe!" }

			throw WebsiteAPIException(
					HttpStatusCode.Unauthorized,
					WebsiteUtils.createErrorPayload(LoriWebCode.UNAUTHORIZED, "Missing Authorization Header from Request")
			)
		}

		if (authorizationHeader != com.mrpowergamerbr.loritta.utils.loritta.config.mixer.webhookSecret) {
			logger.error { "Header de Autorização do request não é igual ao nosso!" }

			throw WebsiteAPIException(
					HttpStatusCode.Unauthorized,
					WebsiteUtils.createErrorPayload(LoriWebCode.UNAUTHORIZED, "Missing Authorization Content from Request")
			)
		}

		val payload = JsonParser.parseString(response)
		val botId = payload["bot"].string
		val userId = payload["user"].string.toLong()
		val type = payload["type"].string

		if (type == "upvote" || (type == "test" && com.mrpowergamerbr.loritta.utils.loritta.config.isOwner(userId))) {
			WebsiteVoteUtils.addVote(
					userId,
					WebsiteVoteSource.DISCORD_BOTS
			)
		}

		call.respondText(status = HttpStatusCode.NoContent) { "" }
	}
}