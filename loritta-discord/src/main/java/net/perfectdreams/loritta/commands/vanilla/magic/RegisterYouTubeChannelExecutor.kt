package net.perfectdreams.loritta.commands.vanilla.magic

import com.github.kevinsawicki.http.HttpRequest
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply

object RegisterYouTubeChannelExecutor : LoriToolsCommand.LoriToolsExecutor {
	override val args = "register youtube <channel-id>"

	override fun executes(): suspend CommandContext.() -> Boolean  = task@{
		if (args.getOrNull(0) != "register")
			return@task false
		if (args.getOrNull(1) != "youtube")
			return@task false

		val code = HttpRequest.post("https://pubsubhubbub.appspot.com/subscribe")
				.form(mapOf(
						"hub.callback" to "${com.mrpowergamerbr.loritta.utils.loritta.instanceConfig.loritta.website.url}api/v1/callbacks/pubsubhubbub?type=ytvideo",
						"hub.lease_seconds" to "",
						"hub.mode" to "subscribe",
						"hub.secret" to com.mrpowergamerbr.loritta.utils.loritta.config.generalWebhook.webhookSecret,
						"hub.topic" to "https://www.youtube.com/xml/feeds/videos.xml?channel_id=${args.getOrNull(2)}",
						"hub.verify" to "async",
						"hub.verify_token" to com.mrpowergamerbr.loritta.utils.loritta.config.generalWebhook.webhookSecret
				))
				.code()

		reply(
				LorittaReply(
						"Feito! Code: $code"
				)
		)
		return@task true
	}
}