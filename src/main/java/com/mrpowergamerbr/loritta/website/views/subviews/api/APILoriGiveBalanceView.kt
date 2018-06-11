package com.mrpowergamerbr.loritta.website.views.subviews.api

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save
import org.jooby.Request
import org.jooby.Response

class APILoriGiveBalanceView : NoVarsRequireAuthView() {
	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/economy/give-balance"))
	}

	override fun renderProtected(req: Request, res: Response, path: String): String {
		val json = JsonObject()

		val body = jsonParser.parse(req.body().value()).obj

		val userId = body["userId"].string
		val quantity = body["quantity"].double
		val lorittaProfile = loritta.getLorittaProfileForUser(userId)

		lorittaProfile.dreams = quantity
		loritta save lorittaProfile
		json["balance"] = lorittaProfile.dreams
		return json.toString()
	}
}