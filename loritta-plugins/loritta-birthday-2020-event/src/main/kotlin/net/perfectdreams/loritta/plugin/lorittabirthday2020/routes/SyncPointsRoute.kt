package net.perfectdreams.loritta.plugin.lorittabirthday2020.routes

import com.github.salomonbrys.kotson.jsonObject
import com.mrpowergamerbr.loritta.Loritta
import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.plugin.lorittabirthday2020.LorittaBirthday2020
import net.perfectdreams.loritta.plugin.lorittabirthday2020.LorittaBirthday2020Event
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson

class SyncPointsRoute(val m: LorittaBirthday2020Event, loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/birthday-2020/sync-points/{userId}/{type}") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		loritta as Loritta
		val type = call.parameters["type"]!!
		LorittaBirthday2020.sendPresentCount(m, call.parameters["userId"]!!.toLong(), type)
		call.respondJson(jsonObject())
	}
}