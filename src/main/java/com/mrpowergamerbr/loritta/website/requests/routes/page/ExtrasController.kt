package com.mrpowergamerbr.loritta.website.requests.routes.page

import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import com.mrpowergamerbr.loritta.website.evaluate
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path

@Path("/:localeId/extras")
class ExtrasController {
	@GET
	@LoriRequiresVariables(true)
	fun handle(req: Request, res: Response, @Local variables: MutableMap<String, Any?>) {
		variables["extraType"] = "default"
		res.send(evaluate("extras.html", variables))
	}
}