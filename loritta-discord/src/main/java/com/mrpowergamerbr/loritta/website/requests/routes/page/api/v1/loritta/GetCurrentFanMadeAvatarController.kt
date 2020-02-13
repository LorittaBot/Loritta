package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.loritta

import com.github.salomonbrys.kotson.jsonObject
import com.mrpowergamerbr.loritta.threads.UpdateStatusThread
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Path

@Path("/api/v1/loritta/current-fan-art-avatar")
class GetCurrentFanMadeAvatarController {
	@GET
	@LoriDoNotLocaleRedirect(true)
	fun handle(req: Request, res: Response) {
		res.type(MediaType.json)

		val currentFanArt = UpdateStatusThread.currentFanArt

		if (currentFanArt != null) {
			res.send(
					jsonObject(
							"artistId" to currentFanArt.artistId,
							"fancyName" to currentFanArt.fancyName,
							"fileName" to currentFanArt.fileName
					)
			)
		} else {
			res.send(
					jsonObject()
			)
		}
	}
}