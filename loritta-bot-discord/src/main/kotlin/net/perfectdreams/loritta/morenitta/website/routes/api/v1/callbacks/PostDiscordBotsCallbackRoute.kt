package net.perfectdreams.loritta.morenitta.website.routes.api.v1.callbacks

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.common.utils.LegacyWebsiteVoteSource
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.WebsiteVoteUtils
import net.perfectdreams.loritta.morenitta.website.LoriWebCode
import net.perfectdreams.loritta.morenitta.website.WebsiteAPIException
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.sequins.ktor.BaseRoute

class PostDiscordBotsCallbackRoute(val loritta: LorittaBot) : BaseRoute("/api/v1/callbacks/discord-bots") {
	companion object {
		private val logger by HarmonyLoggerFactory.logger {}
	}

	override suspend fun onRequest(call: ApplicationCall) {
		val response = withContext(Dispatchers.IO) { call.receiveText() }

		logger.info { "Recebi payload do Discord Bots!" }
		logger.trace { response }

		val authorizationHeader = call.request.header("Authorization")
		if (authorizationHeader == null) {
			logger.error { "Header de Autorização do request não existe!" }

			throw WebsiteAPIException(
					HttpStatusCode.Unauthorized,
					WebsiteUtils.createErrorPayload(loritta, LoriWebCode.UNAUTHORIZED, "Missing Authorization Header from Request")
			)
		}

		if (authorizationHeader != loritta.config.loritta.webhookSecret) {
			logger.error { "Header de Autorização do request não é igual ao nosso!" }

			throw WebsiteAPIException(
					HttpStatusCode.Unauthorized,
					WebsiteUtils.createErrorPayload(loritta, LoriWebCode.UNAUTHORIZED, "Missing Authorization Content from Request")
			)
		}

		val payload = JsonParser.parseString(response)
		val botId = payload["bot"].string
		val userId = payload["user"].string.toLong()
		val type = payload["type"].string

		if (type == "upvote" || (type == "test" && loritta.isOwner(userId))) {
			// We need to run this in a separate thread to avoid top.gg timing out and repeating the request multiple times
			GlobalScope.launch(loritta.coroutineDispatcher) {
				WebsiteVoteUtils.addVote(
					loritta,
					userId,
					LegacyWebsiteVoteSource.DISCORD_BOTS
				)
			}
		}

		call.respondText(status = HttpStatusCode.NoContent) { "" }
	}
}