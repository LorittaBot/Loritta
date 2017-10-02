package com.mrpowergamerbr.loritta.frontend.views.subviews

import com.mrpowergamerbr.loritta.frontend.evaluate
import org.jooby.Request
import org.jooby.Response

class HomeView : AbstractView() {
	override fun handleRender(req: Request, res: Response): Boolean {
		return req.path() == "/"
	}

	override fun render(req: Request, res: Response): String {
		return evaluate("home.html")
	}
}