package net.pocketdreams.loriplugins.simsimi.commands

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.JSON_PARSER
import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.getOrCreateWebhook
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import java.net.URLEncoder

class SimsimiCommand : CommandBase("simsimi") {
	override fun getDescription(locale: BaseLocale): String = locale["SIMSIMI_DESCRIPTION"]

	override fun getExample(): List<String> = listOf("Como vai você?")

	override fun getCategory(): CommandCategory = CommandCategory.FUN

	override fun hasCommandFeedback(): Boolean = false

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val query = context.args.joinToString(" ")
			var locale = "pt"
			if (context.config.localeId == "en-us") {
				locale = "en"
			}
			// {"response":"Claro que é, aquele delícia *-*","id":"49383506","result":100,"msg":"OK."}
			val get = HttpRequest.get("http://api.simsimi.com/request.p?key=${Loritta.config.simsimiKey}&lc=$locale&ft=1.0&text=${URLEncoder.encode(query, "UTF-8")}")
					.body()

			val jsonElement = JSON_PARSER.parse(get)
			if (!jsonElement.isJsonNull) {
				val json = JSON_PARSER.parse(get).obj

				if (json.has("response")) {
					val response = json["response"].string
							.escapeMentions()

					val webhook = getOrCreateWebhook(context.event.textChannel, "Simsimi")
					context.sendMessage(webhook, DiscordMessage.builder()
							.username(context.locale["SIMSIMI_NAME"])
							.content(context.getAsMention(true) + response)
							.avatarUrl("https://loritta.website/assets/img/simsimi_face.png?v=3")
							.build())
					return
				}
			}
			var text = loritta.hal.sentence

			text = if (text.length > 400) text.substring(0, 400) + "..." else text
			val webhook = getOrCreateWebhook(context.event.textChannel, "Frase Tosca")
			context.sendMessage(webhook, DiscordMessage.builder()
					.username(context.locale.get("FRASETOSCA_GABRIELA"))
					.content(context.getAsMention(true) + context.locale.get("SIMSIMI_FAIL") + text)
					.avatarUrl("http://i.imgur.com/aATogAg.png")
					.build())
		} else {
			context.explain()
		}
	}
}