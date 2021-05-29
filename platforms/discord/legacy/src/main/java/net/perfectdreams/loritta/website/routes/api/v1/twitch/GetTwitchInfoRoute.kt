package net.perfectdreams.loritta.website.routes.api.v1.twitch

import com.github.salomonbrys.kotson.jsonObject
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson

class GetTwitchInfoRoute(val loritta: LorittaDiscord) : BaseRoute("/api/v1/twitch/channel") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onRequest(call: ApplicationCall) {
		val id = call.parameters["id"]?.toLongOrNull()
		val login = call.parameters["login"]
		
		if (id != null) {
			val payload = com.mrpowergamerbr.loritta.utils.loritta.twitch.getUserLoginById(id)
					?: throw WebsiteAPIException(
							HttpStatusCode.NotFound,
							WebsiteUtils.createErrorPayload(
									LoriWebCode.ITEM_NOT_FOUND,
									"Streamer not found"
							)
					)

			call.respondJson(gson.toJsonTree(payload))
		} else if (login != null) {
			val payload = com.mrpowergamerbr.loritta.utils.loritta.twitch.getUserLogin(login)
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