package com.mrpowergamerbr.loritta.frontend.views.subviews

import com.mrpowergamerbr.loritta.frontend.evaluate
import org.jooby.Request
import org.jooby.Response

class ServersView : AbstractView() {
	override fun handleRender(req: Request, res: Response, variables: MutableMap<String, Any?>): Boolean {
		return req.path().startsWith("/servers")
	}

	override fun render(req: Request, res: Response, variables: MutableMap<String, Any?>): String {
		val args = req.path().split("/")
		val arg2 = args.getOrNull(2)

		if (arg2 == "faq") {
			return evaluate("sponsored_faq.html", variables)
		}

		return evaluate("server_list.html", variables)
	}
}