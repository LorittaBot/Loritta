package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.loritta

import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import org.jooby.mvc.GET
import org.jooby.mvc.Path

@Path("/api/v1/loritta/user/:userId/send-help")
class SendHelpController {
	@GET
	@LoriDoNotLocaleRedirect(true)
	fun handle(req: Request, res: Response, userId: String) {
		res.type(MediaType.json)

		val user = lorittaShards.getUserById(userId)

		if (user == null) {
			res.status(Status.NOT_FOUND)
			res.send("")
			return
		}

		res.status(Status.ACCEPTED)
		res.send("")
	}
}