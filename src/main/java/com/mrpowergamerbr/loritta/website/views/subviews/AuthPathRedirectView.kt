package com.mrpowergamerbr.loritta.website.views.subviews

import com.mrpowergamerbr.loritta.Loritta
import org.jooby.Request
import org.jooby.Response

class AuthPathRedirectView : AbstractView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		return path == "/auth"
	}

	override fun render(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): String {
		res.redirect(Loritta.config.websiteUrl + "dashboard")
		return "Redirecionando..."
	}

}