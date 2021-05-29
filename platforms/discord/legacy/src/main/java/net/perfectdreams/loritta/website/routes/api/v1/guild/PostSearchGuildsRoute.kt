package net.perfectdreams.loritta.website.routes.api.v1.guild

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toJsonArray
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.*
import io.ktor.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson

class PostSearchGuildsRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/guilds/search") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val body = withContext(Dispatchers.IO) { call.receiveText() }
		val json = JsonParser.parseString(body)
		val pattern = json["pattern"].string

		val regex = Regex(pattern)

		val array = lorittaShards.getGuilds()
				.filter { it.name.contains(regex) }
				.map {
					jsonObject(
							"id" to it.idLong,
							"name" to it.name
					)
				}
				.toJsonArray()

		call.respondJson(array)
	}
}