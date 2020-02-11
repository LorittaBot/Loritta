package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.callbacks

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import mu.KotlinLogging
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Path
import java.io.File
import java.net.URL
import kotlin.concurrent.thread

@Path("/api/v1/callbacks/update-available")
class UpdateAvailableCallbackController {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	@GET
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.API_KEY)
	fun handle(req: Request, res: Response): String {
		thread {
			Thread.sleep(5000)
			val body = HttpRequest.get("https://jenkins.perfectdreams.net/job/Loritta/lastSuccessfulBuild/api/json")
					.userAgent(Constants.USER_AGENT)
					.body()

			val payload = jsonParser.parse(body).obj

			val items = payload["changeSet"]["items"].array

			val artifacts = payload["artifacts"].array
			val firstDiscordArtifact = artifacts.firstOrNull { it["fileName"].string.startsWith("loritta-discord-fat-") } ?: return@thread

			run {
				val textChannel = lorittaShards.getTextChannelById(Constants.PORTUGUESE_SUPPORT_GUILD_ID)

				if (textChannel != null) {
					val loriReplies = mutableListOf<LoriReply>()

					loriReplies.add(
							LoriReply(
									"Chegou novidades para mim \uD83D\uDCE6\uD83D\uDC40, deixa eu ir ver o que é!",
									"<:lori_owo:417813932380520448>"
							)
					)

					if (items.size() == 0) {
						loriReplies.add(
								LoriReply(
										"Nada de novo (apenas um rebuild)... Mas mesmo assim eu irei dar uma voltinha para eu descansar um pouco!",
										"<:lori_triste:370344565967814659>"
								)
						)
					} else {
						items.forEach {
							loriReplies.add(
									LoriReply(
											"Novidade: `${it["comment"].string.stripNewLines()}`",
											"<a:revolving_think:417382964364836864>"
									)
							)
						}
					}

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

			val relativePath = firstDiscordArtifact["relativePath"].string

			val byteArray = URL("https://jenkins.perfectdreams.net/job/Loritta/lastSuccessfulBuild/artifact/$relativePath")
					.readBytes()

			File(Loritta.FOLDER, "Loritta-Update.jar").writeBytes(byteArray)

			logger.info("Recebi que um update está disponível no Jenkins! Irei reiniciar para aplicar as novas mudanças recebidas!!!")
			loritta.website.stop()
			lorittaShards.shardManager.shutdown()
			System.exit(0)
		}

		return "{}"
	}
}