package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.loritta

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.utils.PatchData
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import mu.KotlinLogging
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.Body
import org.jooby.mvc.GET
import org.jooby.mvc.POST
import org.jooby.mvc.Path
import kotlin.concurrent.thread
import kotlin.system.exitProcess

@Path("/api/v1/loritta/update")
class UpdateReadyController {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	@GET
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.API_KEY)
	fun handle(req: Request, res: Response) {
		res.type(MediaType.json)
		logger.info { "Received request to restart, waiting 2.5s and then shutting down..." }

		thread {
			Thread.sleep(2_500)
			exitProcess(0)
		}

		res.send("")
	}

	@POST
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.API_KEY)
	fun handle(req: Request, res: Response, @Body body: String) {
		res.type(MediaType.json)

		val json = jsonParser.parse(body)

		val type = json["type"].string

		when (type) {
			"setRestartTimer" -> {
				val willRestartAt = json["willRestartAt"].long

				loritta.patchData.willRestartAt = willRestartAt
				res.send("")
			}
			"setPatchNotesPost" -> {
				val patchNotesPostId = json["patchNotesPostId"].string
				val expiresAt = json["expiresAt"].long

				loritta.patchData.patchNotes = PatchData.PatchNotes(
						System.currentTimeMillis(),
						expiresAt,
						patchNotesPostId
				)

				res.send("")
			}
		}
	}
}