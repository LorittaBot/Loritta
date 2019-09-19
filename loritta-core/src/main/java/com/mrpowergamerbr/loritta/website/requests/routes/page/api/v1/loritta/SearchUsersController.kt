package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.loritta

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toJsonArray
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.Body
import org.jooby.mvc.POST
import org.jooby.mvc.Path

@Path("/api/v1/loritta/user/search")
class SearchUsersController {
	@POST
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.API_KEY)
	fun handle(req: Request, res: Response, @Body body: String) {
		res.type(MediaType.json)

		val json = jsonParser.parse(body)
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

		res.send(array)
	}
}