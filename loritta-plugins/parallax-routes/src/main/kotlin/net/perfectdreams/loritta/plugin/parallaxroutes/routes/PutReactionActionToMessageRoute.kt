package net.perfectdreams.loritta.plugin.parallaxroutes.routes

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.MessageInteractionFunctions
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.ApplicationCall
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.userAgent
import io.ktor.request.receiveText
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.utils.NetAddressUtils
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import java.util.*
import kotlin.collections.set

class PutReactionActionToMessageRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/parallax/channels/{channelId}/messages/{messageId}/reactions/{emoji}/action") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		loritta as Loritta
		try {
			val channelId = call.parameters["channelId"]!!
			val messageId = call.parameters["messageId"]!!
			val emoji = call.parameters["emoji"]!!
			val payload = call.receiveText()
			val json = JsonParser.parseString(payload)

			val channel = lorittaShards.getTextChannelById(channelId)!!

			val actionType = json["actionType"].string
			val trackingId = UUID.fromString(json["trackingId"].string)

			loritta.messageInteractionCache[messageId.toLong()] = MessageInteractionFunctions(
					channel.guild.idLong,
					channel.idLong,
					json["userId"].long
			).apply {
				if (actionType == "onReactionAddByAuthor") {
					onReactionAddByAuthor = {
						loritta.http.get<HttpResponse>("http://${NetAddressUtils.fixIp(com.mrpowergamerbr.loritta.utils.loritta.config.parallaxCodeServer.url)}/api/v1/parallax/reactions/callback/$trackingId") {
							userAgent(com.mrpowergamerbr.loritta.utils.loritta.lorittaCluster.getUserAgent())
						}
					}
				}
			}

			call.respondJson(jsonObject())
		} catch (e: Throwable) {
			e.printStackTrace()
		}
	}
}