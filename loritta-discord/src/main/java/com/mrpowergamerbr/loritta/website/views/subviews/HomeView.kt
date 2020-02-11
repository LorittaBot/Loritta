package com.mrpowergamerbr.loritta.website.views.subviews

import com.mrpowergamerbr.loritta.website.evaluate
import org.jooby.Request
import org.jooby.Response

class HomeView : AbstractView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		return path == "/"
	}

	override fun render(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): String {
		return evaluate("home.html", variables)
	}
}