package com.mrpowergamerbr.loritta.website.views.subviews

import com.mrpowergamerbr.loritta.utils.loritta
import org.jooby.Request
import org.jooby.Response

class AuthPathRedirectView : AbstractView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		return path == "/auth"
	}

	override fun render(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): String {
		res.redirect(loritta.instanceConfig.loritta.website.url + "dashboard")
		return "Redirecionando..."
	}

}