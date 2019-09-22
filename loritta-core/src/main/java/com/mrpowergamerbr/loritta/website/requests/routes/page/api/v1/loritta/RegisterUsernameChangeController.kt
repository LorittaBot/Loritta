package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.loritta

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.mrpowergamerbr.loritta.listeners.EventLogListener
import com.mrpowergamerbr.loritta.utils.jsonParser
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
import kotlin.collections.set

@Path("/api/v1/loritta/user/:userId/username-change")
class RegisterUsernameChangeController {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	@POST
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.API_KEY)
	fun handle(req: Request, res: Response, userId: String, @Body payload: String) {
		res.type(MediaType.json)

		val json = jsonParser.parse(payload).obj

		val name = json["name"].nullString
		val discriminator = json["discriminator"].nullString

		logger.info { "Received user info change for $userId! name = $name; discriminator = $discriminator"}

		val userIdAsLong = userId.toLong()

		if (!EventLogListener.handledUsernameChanges.containsKey(userIdAsLong)) {
			EventLogListener.handledUsernameChanges[userIdAsLong] = EventLogListener.UserMetaHolder(name, discriminator)
		} else {
			val usernameChange = EventLogListener.handledUsernameChanges[userIdAsLong]!!
			usernameChange.oldName = name
			usernameChange.oldDiscriminator = discriminator
		}

		res.send(jsonObject())
	}
}