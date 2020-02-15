package net.perfectdreams.loritta.website.routes.api.v1.loritta

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.utils.PatchData
import com.mrpowergamerbr.loritta.utils.jsonParser
import io.ktor.application.ApplicationCall
import io.ktor.request.receiveText
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson

class PostUpdateReadyRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/loritta/update") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val json = jsonParser.parse(call.receiveText())

		val type = json["type"].string

		when (type) {
			"setRestartTimer" -> {
				val willRestartAt = json["willRestartAt"].long

				com.mrpowergamerbr.loritta.utils.loritta.patchData.willRestartAt = willRestartAt
				call.respondJson(jsonObject())
			}
			"setPatchNotesPost" -> {
				val patchNotesPostId = json["patchNotesPostId"].string
				val expiresAt = json["expiresAt"].long

				com.mrpowergamerbr.loritta.utils.loritta.patchData.patchNotes = PatchData.PatchNotes(
						System.currentTimeMillis(),
						expiresAt,
						patchNotesPostId
				)

				call.respondJson(jsonObject())
			}
		}
	}
}