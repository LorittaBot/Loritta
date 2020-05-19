package net.perfectdreams.loritta.plugin.parallaxroutes.routes

import com.github.salomonbrys.kotson.jsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.ApplicationCall
import io.ktor.http.decodeURLQueryComponent
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson

class PutReactionToMessageRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/parallax/channels/{channelId}/messages/{messageId}/reactions/{emoji}/@me") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		loritta as Loritta
		try {
			val channelId = call.parameters["channelId"]!!
			val messageId = call.parameters["messageId"]!!
			val emoji = call.parameters["emoji"]!!

			val channel = lorittaShards.getTextChannelById(channelId)!!

			channel.addReactionById(messageId, emoji.decodeURLQueryComponent()).await()
			call.respondJson(jsonObject())
		} catch (e: Throwable) {
			e.printStackTrace()
		}
	}
}