package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.loritta

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import net.perfectdreams.loritta.utils.extensions.objectNode
import net.perfectdreams.loritta.utils.extensions.set
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import org.jooby.mvc.GET
import org.jooby.mvc.Path

@Path("/api/v1/loritta/locale/:localeId")
class GetLocaleController {
	@GET
	@LoriDoNotLocaleRedirect(true)
	fun handle(req: Request, res: Response, localeId: String) {
		res.type(MediaType.json)
		res.status(Status.OK)

		val locale = loritta.locales[localeId] ?: loritta.locales["default"]!!

		val localeEntries = objectNode()
		locale.localeEntries.forEach {
			val value = it.value

			if (value is String) {
				localeEntries[it.key] = value
			}
		}

		val node = objectNode(
				"id" to locale.id,
				"path" to locale.path,
				"localeEntries" to localeEntries
		)

		res.send(
				Constants.JSON_MAPPER.writeValueAsString(node)
		)
	}
}