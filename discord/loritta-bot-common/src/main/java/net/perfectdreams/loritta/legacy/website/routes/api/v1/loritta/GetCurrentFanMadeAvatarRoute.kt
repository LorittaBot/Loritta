package net.perfectdreams.loritta.legacy.website.routes.api.v1.loritta

import com.github.salomonbrys.kotson.jsonObject
import net.perfectdreams.loritta.legacy.threads.UpdateStatusThread
import io.ktor.server.application.ApplicationCall
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.loritta.legacy.website.utils.extensions.respondJson

class GetCurrentFanMadeAvatarRoute(val loritta: LorittaDiscord) : BaseRoute("/api/v1/loritta/current-fan-art-avatar") {
	override suspend fun onRequest(call: ApplicationCall) {
		val currentFanArt = UpdateStatusThread.currentFanArt

		if (currentFanArt != null) {
			call.respondJson(
					jsonObject(
							"artistId" to currentFanArt.artistId,
							"fancyName" to currentFanArt.fancyName,
							"fileName" to currentFanArt.fileName
					)
			)
		} else {
			call.respondJson(
					jsonObject()
			)
		}
	}
}