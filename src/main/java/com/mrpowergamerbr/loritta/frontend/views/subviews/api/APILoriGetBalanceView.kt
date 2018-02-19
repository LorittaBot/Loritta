package com.mrpowergamerbr.loritta.frontend.views.subviews.api

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.frontend.views.subviews.AbstractView
import com.mrpowergamerbr.loritta.utils.JSON_PARSER
import com.mrpowergamerbr.loritta.utils.MiscUtils.getResponseError
import com.mrpowergamerbr.loritta.utils.loritta
import org.jooby.Request
import org.jooby.Response

class APILoriGetBalanceView : NoVarsRequireAuthView() {
	override fun handleRender(req: Request, res: Response): Boolean {
		return req.path().matches(Regex("^/api/v1/economy/get-balance"))
	}

	override fun renderProtected(req: Request, res: Response): String {
		val json = JsonObject()

		val body = JSON_PARSER.parse(req.body().value()).obj

		val userId = body["userId"].string

		val lorittaProfile = loritta.getLorittaProfileForUser(userId)

		json["balance"] = lorittaProfile.dreams
		return json.toString()
	}
}