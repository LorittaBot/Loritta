package com.mrpowergamerbr.loritta.website.views.subviews.api

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.Request
import org.jooby.Response

class APILoriSetBalanceView : NoVarsRequireAuthView() {
	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/economy/set-balance"))
	}

	override fun renderProtected(req: Request, res: Response, path: String): String {
		val json = JsonObject()

		val body = jsonParser.parse(req.body().value()).obj

		val userId = body["userId"].string
		val quantity = body["quantity"].double
		val lorittaProfile = loritta.getOrCreateLorittaProfile(userId)

		transaction(Databases.loritta) {
			lorittaProfile.money = quantity
		}
		loritta save lorittaProfile
		json["balance"] = quantity
		return json.toString()
	}
}