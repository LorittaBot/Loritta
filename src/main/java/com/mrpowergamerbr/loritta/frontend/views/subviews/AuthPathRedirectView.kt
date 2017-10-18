package com.mrpowergamerbr.loritta.frontend.views.subviews

import com.mrpowergamerbr.loritta.Loritta
import org.jooby.Request
import org.jooby.Response

class AuthPathRedirectView : AbstractView() {
	override fun handleRender(req: Request, res: Response, variables: MutableMap<String, Any?>): Boolean {
		return req.path() == "/auth"
	}

	override fun render(req: Request, res: Response, variables: MutableMap<String, Any?>): String {
		res.redirect(Loritta.config.websiteUrl + "dashboard")
		return "Redirecionando..."
	}

}