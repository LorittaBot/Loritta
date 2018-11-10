package com.mrpowergamerbr.loritta.website.requests.routes.page.user

import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import com.mrpowergamerbr.loritta.website.evaluateKotlin
import kotlinx.html.body
import kotlinx.html.html
import kotlinx.html.stream.appendHTML
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path

@Path("/:localeId/user/:userId/rep")
class UserReputationController {
	@GET
	@LoriRequiresVariables(true)
	fun handle(req: Request, res: Response, @Local variables: MutableMap<String, Any?>): String {
		val result = evaluateKotlin("user/reputation.kts", "onLoad", "uwu")
		return StringBuilder().apply { appendHTML().html { body { result.invoke(this) } }}.toString()
	}
}