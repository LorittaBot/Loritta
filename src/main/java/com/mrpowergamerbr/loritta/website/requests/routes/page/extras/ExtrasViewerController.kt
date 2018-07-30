package com.mrpowergamerbr.loritta.website.requests.routes.page.extras

import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import com.mrpowergamerbr.loritta.website.evaluate
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path
import java.io.File

@Path("/:localeId/extras/:pageId")
class ExtrasViewerController {
	@GET
	@LoriRequiresVariables(true)
	fun handle(req: Request, res: Response, @Local variables: MutableMap<String, Any?>) {
		val extraType = req.path().split("/").getOrNull(2)?.replace(".", "")?.replace("/", "")

		if (extraType != null) {
			if (File(LorittaWebsite.FOLDER, "extras/$extraType.html").exists()) {
				return res.send(evaluate("extras/$extraType.html", variables))
			}
		}

		res.status(404)
		return res.send(evaluate("404.html"))
	}
}