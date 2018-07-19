package com.mrpowergamerbr.loritta.website.views.subviews.api

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.website.LoriWebCodes
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save
import org.jooby.Request
import org.jooby.Response

class APILoriWithdrawBalanceView : NoVarsRequireAuthView() {
	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/economy/withdraw-balance"))
	}

	override fun renderProtected(req: Request, res: Response, path: String): String {
		val json = JsonObject()

		val body = jsonParser.parse(req.body().value()).obj

		val userId = body["userId"].string
		val quantity = body["quantity"].double
		val reason = body["reason"].string
		val guildId = body["guildId"].string

		val lorittaProfile = loritta.getLorittaProfileForUser(userId)

		if (quantity.isNaN()) {
			json["api:message"] = "Not a number"
			json["api:code"] = LoriWebCodes.UNAUTHORIZED
			return json.toString()
		}

		if (0 >= quantity) {
			json["api:message"] = "Trying to withdraw less or equal to zero amount"
			json["api:code"] = LoriWebCodes.UNAUTHORIZED
			return json.toString()
		}

		if (quantity > lorittaProfile.dreams) {
			json["api:message"] = "INSUFFICIENT_FUNDS"
			json["api:code"] = LoriWebCodes.INSUFFICIENT_FUNDS
			return json.toString()
		}

		val before = lorittaProfile.dreams

		lorittaProfile.dreams -= quantity
		loritta save lorittaProfile
		json["balance"] = lorittaProfile.dreams
		json["api:code"] = LoriWebCodes.SUCCESS

		Loritta.logger.info("${lorittaProfile.userId} teve $quantity sonhos removidos (antes possuia $before sonhos), motivo: ${reason} - ID: ${guildId}")
		return json.toString()
	}
}