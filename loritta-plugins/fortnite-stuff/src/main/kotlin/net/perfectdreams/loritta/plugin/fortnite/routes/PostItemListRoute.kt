package net.perfectdreams.loritta.plugin.fortnite.routes

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonParser
import io.ktor.application.ApplicationCall
import io.ktor.request.receiveText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.plugin.fortnite.FortniteStuff
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import kotlin.collections.set

class PostItemListRoute(val m: FortniteStuff, loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/fortnite/items/{localeId}") {
	private val logger = KotlinLogging.logger {}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val payload = withContext(Dispatchers.IO) { JsonParser.parseString(call.receiveText()) }
		logger.info { "Received Fortnite Item Payload! localeId is ${call.parameters["localeId"]}" }

		m.itemsInfo[call.parameters["localeId"]!!] = payload["data"].array

		call.respondJson(jsonObject())
	}
}