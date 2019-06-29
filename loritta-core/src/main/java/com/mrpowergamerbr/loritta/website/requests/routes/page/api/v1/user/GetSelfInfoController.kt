package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.user

import com.github.salomonbrys.kotson.jsonObject
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.website.*
import mu.KotlinLogging
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import org.jooby.mvc.GET
import org.jooby.mvc.Path

@Path("/api/v1/users/@me")
class GetSelfInfoController {
	private val logger = KotlinLogging.logger {}

	@GET
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresVariables(true)
	@LoriForceReauthentication(true)
	fun getUserProfile(req: Request, res: Response) {
		res.type(MediaType.json)

		val userIdentification = req.attributes()["userIdentification"] as TemmieDiscordAuth.UserIdentification? ?: throw WebsiteAPIException(Status.UNAUTHORIZED,
				WebsiteUtils.createErrorPayload(
						LoriWebCode.UNAUTHORIZED
				)
		)

		res.send(
				gson.toJson(
						jsonObject(
								"id" to userIdentification.id,
								"username" to userIdentification.username,
								"discriminator" to userIdentification.discriminator,
								"avatar" to userIdentification.avatar,
								"bot" to userIdentification.bot,
								"mfaEnabled" to userIdentification.mfaEnabled,
								"locale" to userIdentification.locale,
								"verified" to userIdentification.verified,
								"email" to userIdentification.email,
								"flags" to userIdentification.flags,
								"premiumType" to userIdentification.premiumType
						)
				)
		)
	}
}