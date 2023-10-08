package net.perfectdreams.loritta.morenitta.website.routes.api.v1.user

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import io.ktor.server.application.ApplicationCall
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson

class GetMutualGuildsRoute(loritta: LorittaBot) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/users/{userId}/mutual-guilds") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val userId = call.parameters["userId"] ?: return

		val user = loritta.lorittaShards.getUserById(userId)

		if (user == null) {
			call.respondJson(
					jsonObject(
							"guilds" to jsonArray()
					)
			)
			return
		}

		val mutualGuilds = loritta.lorittaShards.getMutualGuilds(user)

		call.respondJson(
				jsonObject(
						"guilds" to mutualGuilds.map {
							val member = it.getMember(user)

							jsonObject(
									"id" to it.id,
									"name" to it.name,
									"iconUrl" to it.iconUrl,
									"memberCount" to it.memberCount,
									"timeJoined" to member?.timeJoined?.toInstant()?.toEpochMilli()
							)
						}.toJsonArray()
				)
		)
	}
}