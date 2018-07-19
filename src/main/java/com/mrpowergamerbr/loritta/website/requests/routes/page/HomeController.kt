package com.mrpowergamerbr.loritta.website.requests.routes.page

import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import com.mrpowergamerbr.loritta.website.evaluate
import org.jooby.Request
import org.jooby.Response
import org.jooby.Route
import org.jooby.mvc.GET
import org.jooby.mvc.Path

@Path("/:localeId")
class HomeController {
	@GET
	@LoriRequiresVariables(true)
	fun handle(req: Request, res: Response, chain: Route.Chain, localeId: String) {
		if (localeId == "translation") {
			chain.next(req, res)
			return
		}

		val variables = req.get<MutableMap<String, Any?>>("variables")
		res.send(evaluate("home.html", variables))
	}
}