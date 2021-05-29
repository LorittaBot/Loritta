package net.perfectdreams.loritta.website.routes.api.v1.user

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson

class GetMutualGuildsRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/users/{userId}/mutual-guilds") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val userId = call.parameters["userId"] ?: return

		val user = lorittaShards.getUserById(userId)

		if (user == null) {
			call.respondJson(
					jsonObject(
							"guilds" to jsonArray()
					)
			)
			return
		}

		val mutualGuilds = lorittaShards.getMutualGuilds(user)

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