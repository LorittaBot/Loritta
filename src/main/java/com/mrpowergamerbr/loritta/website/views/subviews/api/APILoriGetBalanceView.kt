package com.mrpowergamerbr.loritta.website.views.subviews.api

import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.website.LoriWebCodes
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import org.jooby.Request
import org.jooby.Response

class APILoriGetBalanceView : NoVarsRequireAuthView() {
	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/economy/get-balance"))
	}

	override fun renderProtected(req: Request, res: Response, path: String): String {
		val json = JsonObject()

		val body = jsonParser.parse(req.body().value()).obj

		val userId = body["userId"].string

		val lorittaProfile = loritta.getLorittaProfileForUser(userId)

		json["balance"] = lorittaProfile.dreams
		json["api:code"] = LoriWebCodes.SUCCESS
		return json.toString()
	}
}