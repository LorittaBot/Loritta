package com.mrpowergamerbr.loritta.frontend.views.subviews

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.frontend.evaluate
import com.mrpowergamerbr.loritta.frontend.views.LoriWebCodes
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import org.jooby.Request
import org.jooby.Response
import java.util.*

class DailyView : AbstractView() {
	override fun handleRender(req: Request, res: Response, variables: MutableMap<String, Any?>): Boolean {
		return req.path().startsWith("/daily")
	}

	override fun render(req: Request, res: Response, variables: MutableMap<String, Any?>): String {
		return evaluate("daily.html", variables)
	}
}