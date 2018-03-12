package com.mrpowergamerbr.loritta.frontend.views.subviews

import com.mrpowergamerbr.loritta.frontend.evaluate
import org.jooby.Request
import org.jooby.Response

class SupportView : AbstractView() {
	override fun handleRender(req: Request, res: Response, variables: MutableMap<String, Any?>): Boolean {
		return req.path() == "/support"
	}

	override fun render(req: Request, res: Response, variables: MutableMap<String, Any?>): String {
		return evaluate("support.html", variables)
	}
}