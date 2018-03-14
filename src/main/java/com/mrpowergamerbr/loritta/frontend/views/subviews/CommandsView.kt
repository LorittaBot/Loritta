package com.mrpowergamerbr.loritta.frontend.views.subviews

import com.mrpowergamerbr.loritta.frontend.evaluate
import org.jooby.Request
import org.jooby.Response

class CommandsView : AbstractView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		return path == "/commands" || path == "/comandos"
	}

	override fun render(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): String {
		return evaluate("commands.html", variables)
	}
}