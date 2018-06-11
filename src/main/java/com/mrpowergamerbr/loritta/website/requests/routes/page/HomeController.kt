package com.mrpowergamerbr.loritta.website.requests.routes.page

import com.mrpowergamerbr.loritta.utils.logger
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path

@Path("/")
class HomeController : InitVariablesController() {
	val logger by logger()

	@GET
	fun handle(req: Request, res: Response, @Local something: Map<String, Any?>): String {
		return "Hello World!!! ${something}"
	}
}