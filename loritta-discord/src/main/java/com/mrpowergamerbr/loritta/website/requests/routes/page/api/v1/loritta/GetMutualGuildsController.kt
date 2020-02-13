package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.loritta

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Path

@Path("/api/v1/loritta/user/:userId/mutual-guilds")
class GetMutualGuildsController {
	@GET
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.API_KEY)
	fun handle(req: Request, res: Response, userId: String) {
		res.type(MediaType.json)

		val user = lorittaShards.getUserById(userId)

		if (user == null) {
			res.send(
					jsonObject(
							"guilds" to jsonArray()
					)
			)
			return
		}

		val mutualGuilds = lorittaShards.getMutualGuilds(user)
				.take(100)

		res.send(
				jsonObject(
						"guilds" to mutualGuilds.map {
							val member = it.getMember(user)

							jsonObject(
									"id" to it.id,
									"name" to it.name,
									"iconUrl" to it.iconUrl,
									"memberCount" to it.memberCache.size(),
									"timeJoined" to member?.timeJoined?.toInstant()?.toEpochMilli()
							)
						}.toJsonArray()
				)
		)
	}
}