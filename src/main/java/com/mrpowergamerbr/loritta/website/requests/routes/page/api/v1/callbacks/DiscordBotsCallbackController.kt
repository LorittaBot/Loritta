package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.callbacks

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.vanilla.social.PerfilCommand
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriWebCode
import mu.KotlinLogging
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import org.jooby.mvc.POST
import org.jooby.mvc.Path

@Path("/api/v1/callbacks/discord-bots")
class DiscordBotsCallbackController {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	@POST
	@LoriDoNotLocaleRedirect(true)
	fun handle(req: Request, res: Response) {
		res.type(MediaType.json)
		val response = req.body().value()

		logger.info("Recebi payload do Discord Bots!")
		logger.trace { response }

		val authorizationHeader = req.header("Authorization")
		if (!authorizationHeader.isSet) {
			logger.error { "Header de Autorização do request não existe!" }
			res.status(Status.UNAUTHORIZED)
			val payload = WebsiteUtils.createErrorPayload(LoriWebCode.UNAUTHORIZED, "Missing Authorization Header from Request")
			res.send(payload.toString())
			return
		}

		val authorization = authorizationHeader.value()
		if (authorization != Loritta.config.mixerWebhookSecret) {
			logger.error { "Header de Autorização do request não é igual ao nosso!" }
			res.status(Status.UNAUTHORIZED)
			val payload = WebsiteUtils.createErrorPayload(LoriWebCode.UNAUTHORIZED, "Invalid Authorization Content from Request")
			res.send(payload.toString())
			return
		}

		val payload = jsonParser.parse(response)
		val botId = payload["bot"].string
		val userId = payload["user"].string
		val type = payload["type"].string

		if (botId == Loritta.config.ownerId && type == "upvote") {
			PerfilCommand.userVotes?.add(PerfilCommand.DiscordBotVote(userId))
		}

		res.status(Status.NO_CONTENT)
		res.send("")
	}
}