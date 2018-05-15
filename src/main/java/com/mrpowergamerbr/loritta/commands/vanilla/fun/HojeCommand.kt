package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import org.jsoup.Jsoup
import java.awt.Color
import java.net.URLEncoder
import java.text.DateFormatSymbols
import java.time.Instant
import java.util.*

class HojeCommand : AbstractCommand("today", listOf("hoje"), CommandCategory.FUN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("TODAY_DESCRIPTION")
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		var languageId = "pt"
		if (context.config.localeId == "en-us") {
			languageId = "en"
		}
		val day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
		val month = Calendar.getInstance().get(Calendar.MONTH)
		val query = "$day de " + DateFormatSymbols().months[month].toLowerCase()
		val url = "https://$languageId.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&redirects=1&titles=" + URLEncoder.encode(query, "UTF-8")
		val wikipediaResponse = HttpRequest.get(url).body()
		val wikipedia = jsonParser.parse(wikipediaResponse).asJsonObject // Base
		val wikiQuery = wikipedia["query"].obj // Query
		val wikiPages = wikiQuery["pages"].obj // Páginas
		val entryWikiContent = wikiPages.entrySet().iterator().next() // Conteúdo

		if (entryWikiContent.key == "-1") { // -1 = Nenhuma página encontrada
			return
		} else {
			// Se não é -1, então é algo que existe! Yay!
			val pageTitle = "\uD83D\uDCC5 ${context.locale.get("TODAY_ON_THIS_DAY")} (${entryWikiContent.value.obj["title"].string})...";
			val pageExtract = entryWikiContent.value.obj.get("extract").string

			val jsoup = Jsoup.parse(pageExtract)

			var headerIndex = 0
			val randomMessages = mutableListOf<String>()
			for (element in jsoup.allElements) {
				if (element.tagName() == "h2") {
					headerIndex++
				}
				if (headerIndex == 2) {
					break
				}
				if (headerIndex == 1 && element.tagName() == "li") {
					if (element.children().filter { it.tagName() == "ul" && it.parent().parent().tagName() == "body" }.count() == 0) {
						var message = ""
						message += "${element.text().replaceFirst(" - ", ": ")}"
						randomMessages.add(message)
					}
				}
			}

			val random = randomMessages.get(Loritta.RANDOM.nextInt(0, randomMessages.size))

			val embed = EmbedBuilder()
					.setTitle(pageTitle, url)
					.setColor(Color(39, 39, 39))
					.setDescription(random)

			embed.setTimestamp(Instant.now())
			context.sendMessage(embed.build()) // Envie a mensagem!
		}
	}
}