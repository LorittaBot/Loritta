package net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.user.PostUserReputationsRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson

class PostReputationMessageRoute(loritta: LorittaBot) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/loritta/send-reputation-message") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val json = withContext(Dispatchers.IO) { JsonParser.parseString(call.receiveText()) }

		val guildId = json["guildId"].string
		val channelId = json["channelId"].string
		val giverId = json["giverId"].string
		val receiverId = json["receiverId"].string
		val reputationCount = json["reputationCount"].int

		val profile = loritta.getOrCreateLorittaProfile(giverId)

		PostUserReputationsRoute.sendReputationReceivedMessage(loritta, guildId, channelId, giverId, profile, receiverId, reputationCount)

		call.respondJson(jsonObject())
	}
}