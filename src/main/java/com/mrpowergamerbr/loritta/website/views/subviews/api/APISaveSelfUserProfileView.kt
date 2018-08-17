package com.mrpowergamerbr.loritta.website.views.subviews.api

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.website.LoriWebCodes
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.utils.save
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response

class APISaveSelfUserProfileView : NoVarsView() {
	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/lori/save-self-profile"))
	}

	override fun render(req: Request, res: Response, path: String): String {
		res.type(MediaType.json)

		if (!req.session().isSet("discordAuth")) {
			return Loritta.GSON.toJson(mapOf("api:code" to LoriWebCodes.UNAUTHORIZED))
		}

		val userIdentification =  try {
			val discordAuth = Loritta.GSON.fromJson<TemmieDiscordAuth>(req.session()["discordAuth"].value())
			discordAuth.isReady(true)
			discordAuth.getUserIdentification() // Vamos pegar qualquer coisa para ver se não irá dar erro
		} catch (e: Exception) {
			return Loritta.GSON.toJson(mapOf("api:code" to LoriWebCodes.UNAUTHORIZED))
		}

		val profile = loritta.getLorittaProfileForUser(userIdentification.id)

		val body = jsonParser.parse(req.body().value()).obj // content payload

		val payload = jsonObject(
				"api:code" to LoriWebCodes.SUCCESS
		)

		val config = body["config"].obj
		profile.aboutMe = config["aboutMe"].string

		loritta save profile

		return GSON.toJson(profile)
	}
}