package com.mrpowergamerbr.loritta.frontend.views.subviews.api

import com.mrpowergamerbr.loritta.frontend.evaluate
import com.mrpowergamerbr.loritta.frontend.views.subviews.AbstractView
import org.jooby.Request
import org.jooby.Response

class CommandsView : AbstractView() {
	override fun handleRender(req: Request, res: Response, variables: MutableMap<String, Any?>): Boolean {
		return req.path() == "/commands" || req.path() == "/comandos"
	}

	override fun render(req: Request, res: Response, variables: MutableMap<String, Any?>): String {
		return evaluate("commands.html", variables)
	}
}