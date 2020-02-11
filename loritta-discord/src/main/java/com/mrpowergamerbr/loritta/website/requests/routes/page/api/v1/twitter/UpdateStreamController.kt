package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.twitter

import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import mu.KotlinLogging
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Path

@Path("/api/v1/twitter/update-stream")
class UpdateStreamController {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	@GET
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.API_KEY)
	fun handle(req: Request, res: Response) {
		res.type(MediaType.json)

		logger.info { "Updating Twitter Stream in the next 15 minutes..." }
		loritta.tweetTracker.restartStream = true
		res.send("{}")
	}
}