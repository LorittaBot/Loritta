package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.user

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.website.*
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import org.jooby.mvc.Body
import org.jooby.mvc.PATCH
import org.jooby.mvc.Path

@Path("/api/v1/user/self-profile")
class SelfProfileController {
	private val logger = KotlinLogging.logger {}

	@PATCH
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresVariables(true)
	@LoriForceReauthentication(true)
	fun updateProfile(req: Request, res: Response, @Body rawMessage: String) {
		res.type(MediaType.json)

		val userIdentification = req.attributes()["userIdentification"] as TemmieDiscordAuth.UserIdentification? ?: throw WebsiteAPIException(Status.UNAUTHORIZED,
				WebsiteUtils.createErrorPayload(
						LoriWebCode.UNAUTHORIZED
				)
		)

		val profile = loritta.getOrCreateLorittaProfile(userIdentification.id)
		val payload = jsonParser.parse(rawMessage)
		val config = payload["config"].obj

		transaction(Databases.loritta) {
			profile.settings.aboutMe = config["aboutMe"].string
		}

		res.send(gson.toJson(jsonObject()))
	}
}