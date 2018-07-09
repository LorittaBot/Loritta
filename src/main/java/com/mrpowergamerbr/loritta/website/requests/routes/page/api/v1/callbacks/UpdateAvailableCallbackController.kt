package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.callbacks

import com.mrpowergamerbr.loritta.utils.logger
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Path

@Path("/api/v1/callbacks/update-available")
class UpdateAvailableCallbackController {
	val logger by logger()

	@GET
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.API_KEY)
	fun handle(req: Request, res: Response): String {
		val guild = lorittaShards.getGuildById("297732013006389252")

		if (guild != null) {
			val textChannel = guild.getTextChannelById("297732013006389252")

			if (textChannel != null) {
				textChannel.sendMessage(
						"Reiniciando..."
				).complete()
			}
		}

		logger.info("Recebi que um update está disponível no Jenkins! Irei reiniciar para aplicar as novas mudanças recebidas!!!")
		System.exit(0)
		return "{}"
	}
}