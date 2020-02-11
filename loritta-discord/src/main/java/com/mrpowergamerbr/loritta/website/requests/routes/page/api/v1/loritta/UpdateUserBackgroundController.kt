package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.loritta

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import mu.KotlinLogging
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.Body
import org.jooby.mvc.POST
import org.jooby.mvc.Path
import java.io.File
import java.util.*

@Path("/api/v1/loritta/users/:userId/background")
class UpdateUserBackgroundController {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	@POST
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.API_KEY)
	fun handle(req: Request, res: Response, userId: String, @Body body: String) {
		res.type(MediaType.json)

		val json = jsonParser.parse(body)

		val type = json["type"].string

		if (type == "custom") {
			logger.info { "Updating $userId background with custom data..." }
			val data = json["data"].string

			File(loritta.instanceConfig.loritta.website.folder, "static/assets/img/backgrounds/${userId}.png")
					.writeBytes(Base64.getDecoder().decode(data))
		}

		res.send(jsonObject())
	}
}