package com.mrpowergamerbr.loritta.website.views.subviews

import com.mrpowergamerbr.loritta.website.evaluate
import org.jooby.Request
import org.jooby.Response

class LorigotchiView : AbstractView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		return path == "/lorigotchi"
	}

	override fun render(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): String {
		return evaluate("lorigotchi.html", variables)
	}
}