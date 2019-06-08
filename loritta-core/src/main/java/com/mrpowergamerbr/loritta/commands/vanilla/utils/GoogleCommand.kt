package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.EmbedBuilder
import org.jsoup.Jsoup
import java.awt.Color
import java.net.URLEncoder


class GoogleCommand : AbstractCommand("google", listOf("g", "search", "procurar", "pesquisar"), CommandCategory.UTILS) {
	override fun getUsage(): String {
		return "pesquisa"
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["GOOGLE_Description"]
	}

	override fun getExamples(): List<String> {
		return listOf("Loritta")
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			val query = context.args.joinToString(" ")

			val safeSearch = "on"

			val httpRequest = HttpRequest.get("https://www.google.com/search?q=${URLEncoder.encode(query, "UTF-8")}&safe=$safeSearch&lr=lang_en&hl=en&ie=utf-8&oe=utf-8&client=firefox-b&gws_rd=cr&dcr=0&ei=lH4EWvLKAoO8wAS64aho")
					.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36")

			val body = httpRequest.body()

			// File("teste.txt").writeText(body)
			val document = Jsoup.parse(body)
			val resultStats = document.getElementById("resultStats").text()
			val elements = document.getElementsByClass("rc")

			val embed = EmbedBuilder().apply {
				setTitle("<:google:378210839171170305> ${context.legacyLocale["YOUTUBE_RESULTS_FOR", query]}")
				setColor(Color(21, 101, 192))
				setFooter(resultStats, null)
			}

			for ((idx, el) in elements.withIndex()) {
				if (idx > 4)
					break

				// context.sendMessage("wow")
				val title = el.getElementsByTag("h3").text()
				val url = el.getElementsByTag("h3")[0].child(0).attr("href")
				val description = el.getElementsByClass("st").text()

				embed.appendDescription("${Constants.INDEXES[idx]} [$title]($url)\n◾ $description\n")
			}

			context.sendMessage(context.getAsMention(true), embed.build())
		} else {
			this.explain(context)
		}
	}
}