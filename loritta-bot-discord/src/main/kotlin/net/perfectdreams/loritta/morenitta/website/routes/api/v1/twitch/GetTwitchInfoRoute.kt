package net.perfectdreams.loritta.morenitta.website.routes.api.v1.twitch

import com.github.salomonbrys.kotson.jsonObject
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.utils.gson
import net.perfectdreams.loritta.morenitta.website.LoriWebCode
import net.perfectdreams.loritta.morenitta.website.WebsiteAPIException
import io.ktor.server.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson

class GetTwitchInfoRoute(val loritta: LorittaBot) : BaseRoute("/api/v1/twitch/channel") {
	companion object {
		private val logger by HarmonyLoggerFactory.logger {}
	}

	override suspend fun onRequest(call: ApplicationCall) {
		val id = call.parameters["id"]?.toLongOrNull()
		val login = call.parameters["login"]
		
		if (id != null) {
			val payload = loritta.twitch.getUserLoginById(id)
					?: throw WebsiteAPIException(
							HttpStatusCode.NotFound,
							WebsiteUtils.createErrorPayload(
									loritta,
									LoriWebCode.ITEM_NOT_FOUND,
									"Streamer not found"
							)
					)

			call.respondJson(gson.toJsonTree(payload))
		} else if (login != null) {
			val payload = loritta.twitch.getUserLogin(login)
					?: throw WebsiteAPIException(
					HttpStatusCode.NotFound,
					WebsiteUtils.createErrorPayload(
							loritta,
							LoriWebCode.ITEM_NOT_FOUND,
							"Streamer not found"
					)
			)

			call.respondJson(gson.toJsonTree(payload))
		} else {
			call.respondJson(jsonObject(), HttpStatusCode.NotImplemented)
		}
	}
}