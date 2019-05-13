package com.mrpowergamerbr.loritta.website.views.subviews

import com.mrpowergamerbr.loritta.website.evaluate
import org.jooby.Request
import org.jooby.Response

class SupportView : AbstractView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		return path == "/support"
	}

	override fun render(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): String {
		return evaluate("support.html", variables)
	}
}