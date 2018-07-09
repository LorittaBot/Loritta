package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.callbacks

import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.logger
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Path
import kotlin.concurrent.thread

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
				val loriReplies = mutableListOf<LoriReply>()

				loriReplies.add(
						LoriReply(
								"Chegou novidades para mim \uD83D\uDCE6\uD83D\uDC40, deixa eu ir ver o que é!",
								"<:lori_owo:417813932380520448>"
						)
				)

				loriReplies.add(
						LoriReply(
								"Novidade: `${req.param("reason").value()}`",
								"<a:revolving_think:417382964364836864>"
						)
				)

				loriReplies.add(
						LoriReply(
								"Daqui a pouco eu já estarei de volta! (e por favor não me xinguem <:notlikemeow:465884453726846987>)",
								"<:lori_yum:414222275223617546>"
						)
				)

				textChannel.sendMessage(
						loriReplies.joinToString("\n", transform = { it.build() })
				).complete()
			}
		}

		logger.info("Recebi que um update está disponível no Jenkins! Irei reiniciar para aplicar as novas mudanças recebidas!!!")

		thread(start = true) {
			Thread.sleep(50)
			loritta.website.stop()
			lorittaShards.shards.forEach {
				it.shutdownNow()
			}
			System.exit(0)
		}

		return "{}"
	}
}