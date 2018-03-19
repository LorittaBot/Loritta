package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.JSON_PARSER
import com.mrpowergamerbr.loritta.utils.encodeToUrl
import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.getOrCreateWebhook
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import java.net.URLEncoder

class SimsimiCommand : AbstractCommand("simsimi", category = CommandCategory.FUN) {
	override fun getDescription(locale: BaseLocale): String = locale["SIMSIMI_DESCRIPTION"]

	override fun getExample(): List<String> = listOf("Como vai você?")

	override fun hasCommandFeedback(): Boolean = false

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val query = context.args.joinToString(" ")
			var locale = "pt"
			if (context.config.localeId == "en-us") {
				locale = "en"
			}
			// {"status":200,"respSentence":"Olá \nTudo bem \nTe amo\nU"}
			val get = HttpRequest.get("https://simsimi.com/getRealtimeReq?lc=$locale&ft=1&normalProb=4&reqText=${query.encodeToUrl()}&status=W&talkCnt=0")
					.header("Cookie", "dotcom_session_key=s%3ADAArvz4yHpchCAf7sOOpKRDB3lJIqQa9.yH9dLgRKVlVeY3TCaDNAMjWWcMmIK%2BWU5VB0ixyVWz0; bbl_cnt=0; normalProb=4; user_displayName=${context.userHandle.name.encodeToUrl()}; user_photo=undefined; lc=pt;")
					.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:61.0) Gecko/20100101 Firefox/61.0")
					.body()

			val jsonElement = JSON_PARSER.parse(get)
			if (!jsonElement.isJsonNull) {
				val json = JSON_PARSER.parse(get).obj

				if (json.has("respSentence")) {
					val response = json["respSentence"].string
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

			val webhook = getOrCreateWebhook(context.event.textChannel, "Frase Tosca")
			context.sendMessage(webhook, DiscordMessage.builder()
					.username(context.locale.get("FRASETOSCA_GABRIELA"))
					.content(context.getAsMention(true) + context.locale["SIMSIMI_FAIL"])
					.avatarUrl("http://i.imgur.com/aATogAg.png")
					.build())
		} else {
			context.explain()
		}
	}
}