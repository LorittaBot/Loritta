package com.mrpowergamerbr.loritta.website.views.subviews

import org.jooby.Request
import org.jooby.Response

abstract class AbstractView {
	abstract fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean

	abstract fun render(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): String
}