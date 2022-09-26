package net.perfectdreams.loritta.legacy.website.routes.api.v1.twitch

import com.github.salomonbrys.kotson.jsonObject
import net.perfectdreams.loritta.legacy.website.utils.WebsiteUtils
import net.perfectdreams.loritta.legacy.utils.gson
import net.perfectdreams.loritta.legacy.website.LoriWebCode
import net.perfectdreams.loritta.legacy.website.WebsiteAPIException
import io.ktor.server.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import mu.KotlinLogging
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.loritta.legacy.website.utils.extensions.respondJson

class GetTwitchInfoRoute(val loritta: LorittaDiscord) : BaseRoute("/api/v1/twitch/channel") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onRequest(call: ApplicationCall) {
		val id = call.parameters["id"]?.toLongOrNull()
		val login = call.parameters["login"]
		
		if (id != null) {
			val payload = net.perfectdreams.loritta.legacy.utils.loritta.twitch.getUserLoginById(id)
					?: throw WebsiteAPIException(
							HttpStatusCode.NotFound,
							WebsiteUtils.createErrorPayload(
									LoriWebCode.ITEM_NOT_FOUND,
									"Streamer not found"
							)
					)

			call.respondJson(gson.toJsonTree(payload))
		} else if (login != null) {
			val payload = net.perfectdreams.loritta.legacy.utils.loritta.twitch.getUserLogin(login)
					?: throw WebsiteAPIException(
					HttpStatusCode.NotFound,
					WebsiteUtils.createErrorPayload(
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