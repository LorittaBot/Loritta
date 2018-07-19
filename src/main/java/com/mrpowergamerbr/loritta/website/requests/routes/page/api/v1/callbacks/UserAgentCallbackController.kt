package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.callbacks

import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Path

@Path("/api/v1/callbacks/user-agent")
class UserAgentCallbackController {
	@GET
	@LoriDoNotLocaleRedirect(true)
	fun handle(req: Request, res: Response): String {
		return req.header("User-Agent").value()
	}
}