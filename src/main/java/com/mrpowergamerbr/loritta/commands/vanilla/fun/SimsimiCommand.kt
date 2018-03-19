package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
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
import org.jsoup.Jsoup
import java.net.URLEncoder

class SimsimiCommand : AbstractCommand("simsimi", category = CommandCategory.FUN) {
	override fun getDescription(locale: BaseLocale): String = locale["SIMSIMI_DESCRIPTION"]

	override fun getExample(): List<String> = listOf("Como vai você?")

	override fun hasCommandFeedback(): Boolean = false

	var currentProxy: Pair<String, Int>? = null

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val query = context.args.joinToString(" ")
			var locale = "pt"
			if (context.config.localeId == "en-us") {
				locale = "en"
			}
			if (currentProxy == null) {
				currentProxy = renewProxy()
			}

			val proxy = currentProxy
			if (proxy == null) {
				logger.info("Cadê o proxy para o Simsimi?!?!?!")
				val webhook = getOrCreateWebhook(context.event.textChannel, "Frase Tosca")
				context.sendMessage(webhook, DiscordMessage.builder()
						.username(context.locale.get("FRASETOSCA_GABRIELA"))
						.content(context.getAsMention(true) + "\uD83E\uDD37")
						.avatarUrl("http://i.imgur.com/aATogAg.png")
						.build())
				return
			}

			logger.info("Usando proxy ${proxy.first}:${proxy.second} para o Simsimi!")
			// {"status":200,"respSentence":"Olá \nTudo bem \nTe amo\nU"}
			var get = HttpRequest.get("https://simsimi.com/getRealtimeReq?lc=$locale&ft=1&normalProb=4&reqText=${query.encodeToUrl()}&status=W&talkCnt=0")
					.useProxy(proxy.first, proxy.second)
					.header("Accept", "application/json, text/javascript, */*; q=0.01")
					.header("Accept-Language", "en-US,en;q=0.5")
					.header("Content-Type", "application/json; charset=utf-8")
					.header("Cookie", "dotcom_session_key=s%3ADAArvz4yHpchCAf7sOOpKRDB3lJIqQa9.yH9dLgRKVlVeY3TCaDNAMjWWcMmIK%2BWU5VB0ixyVWz0; bbl_cnt=0; normalProb=4; user_displayName=Loritta; user_photo=undefined; lc=$locale; lname=Portugu%C3%AAs; currentChatCnt=0; _gat=1")
					.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:61.0) Gecko/20100101 Firefox/61.0")
					.header("Host", "simsimi.com")
					.header("Referer", "https://simsimi.com/")
					.header("X-Requested-With", "XMLHttpRequest")
					.body()

			logger.info(get)
			val jsonElement = JSON_PARSER.parse(get).obj

			if (jsonElement.has("res")) {
				logger.info("Simsimi retornou que fui banido! Renovando proxy...")
				currentProxy = renewProxy()

				if (currentProxy != null) {
					get = HttpRequest.get("https://simsimi.com/getRealtimeReq?lc=$locale&ft=1&normalProb=4&reqText=${query.encodeToUrl()}&status=W&talkCnt=0")
							.useProxy(proxy.first, proxy.second)
							.header("Accept", "application/json, text/javascript, */*; q=0.01")
							.header("Accept-Language", "en-US,en;q=0.5")
							.header("Content-Type", "application/json; charset=utf-8")
							.header("Cookie", "dotcom_session_key=s%3ADAArvz4yHpchCAf7sOOpKRDB3lJIqQa9.yH9dLgRKVlVeY3TCaDNAMjWWcMmIK%2BWU5VB0ixyVWz0; bbl_cnt=0; normalProb=4; user_displayName=Loritta; user_photo=undefined; lc=$locale; lname=Portugu%C3%AAs; currentChatCnt=0; _gat=1")
							.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:61.0) Gecko/20100101 Firefox/61.0")
							.header("Host", "simsimi.com")
							.header("Referer", "https://simsimi.com/")
							.header("X-Requested-With", "XMLHttpRequest")
							.body()
				}
			}

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

	fun renewProxy(): Pair<String, Int>? {
		logger.info("Renovando o proxy do Simsimi!")
		val document = Jsoup.connect("http://www.gatherproxy.com/proxylist/country/?c=Canada")
				.get()

		val classes = document.getElementsByTag("script")

		val firstProxy = classes.firstOrNull { it.html().contains("insertPrx") }

		if (firstProxy != null) {
			val jsonPayload = firstProxy.html().substring(13, firstProxy.html().length - 2)
			val json = JSON_PARSER.parse(jsonPayload)

			logger.info(json.toString())
			return Pair(json["PROXY_IP"].string, Integer.parseInt(json["PROXY_PORT"].string, 16).toInt())
		}
		logger.info("Oh no, nenhum proxy encontrado!")
		return null
	}
}