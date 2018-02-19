package com.mrpowergamerbr.loritta.frontend.views.subviews.api

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.frontend.views.subviews.AbstractView
import com.mrpowergamerbr.loritta.utils.JSON_PARSER
import com.mrpowergamerbr.loritta.utils.MiscUtils.getResponseError
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save
import org.jooby.Request
import org.jooby.Response

class APILoriWithdrawBalanceView : NoVarsRequireAuthView() {
	override fun handleRender(req: Request, res: Response): Boolean {
		return req.path().matches(Regex("^/api/v1/economy/withdraw-balance"))
	}

	override fun renderProtected(req: Request, res: Response): String {
		val json = JsonObject()

		val body = JSON_PARSER.parse(req.body().value()).obj

		val userId = body["userId"].string
		val quantity = body["quantity"].double
		val lorittaProfile = loritta.getLorittaProfileForUser(userId)

		if (quantity > lorittaProfile.dreams) {
			json["api:message"] = "INSUFFICIENT_FUNDS"
			return json.toString()
		}

		lorittaProfile.dreams -= quantity
		loritta save lorittaProfile
		json["balance"] = lorittaProfile.dreams
		return json.toString()
	}
}