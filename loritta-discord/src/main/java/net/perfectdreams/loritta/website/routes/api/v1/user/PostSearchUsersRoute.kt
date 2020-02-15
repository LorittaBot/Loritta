package net.perfectdreams.loritta.website.routes.api.v1.user

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toJsonArray
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.ApplicationCall
import io.ktor.request.receiveText
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson

class PostSearchUsersRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/users/search") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val json = jsonParser.parse(call.receiveText())
		var pattern = json["pattern"].string
		var discriminator: String? = null

		if (pattern.contains("#")) {
			val split = pattern.split("#")
			pattern = split[0]
			discriminator = split[1]
		}

		val regex = Regex(pattern)

		val array = lorittaShards.getUsers()
				.asSequence()
				.filter { if (discriminator != null) { it.discriminator == discriminator } else { true } }
				.filter { it.name.contains(regex) }
				.map {
					jsonObject(
							"id" to it.idLong,
							"name" to it.name,
							"discriminator" to it.discriminator
					)
				}
				.toList().toJsonArray()

		call.respondJson(array)
	}
}