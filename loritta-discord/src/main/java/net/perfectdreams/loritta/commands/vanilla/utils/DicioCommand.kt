package net.perfectdreams.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.utils.Constants
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import org.jsoup.Jsoup
import java.awt.Color
import java.net.URLEncoder

class DicioCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("dicio", "dicionário", "dicionario", "definir"), CommandCategory.UTILS) {
	companion object {
		private const val LOCALE_PREFIX = "commands.utils.dicio"
	}

	override fun command() = create {
		usage {
			argument(ArgumentType.TEXT) {}
		}

		localizedDescription("$LOCALE_PREFIX.description")

		examples {
			listOf("sonho")
		}

		executesDiscord {
			val context = this

			if (args.isNotEmpty()) {
				val word = URLEncoder.encode(context.args[0], "UTF-8")
				val httpRequest = HttpRequest.get("https://www.dicio.com.br/pesquisa.php?q=$word")
						.userAgent(Constants.USER_AGENT)
				val response = httpRequest.body()

				if (httpRequest.code() == 404) {
					context.reply(
							locale["$LOCALE_PREFIX.wordNotFound"],
							Constants.ERROR
					)
					return@executesDiscord
				}

				var jsoup = Jsoup.parse(response)

				val resultsClass = jsoup.getElementsByClass("resultados")
				val results = resultsClass.firstOrNull()

				if (results != null) {
					val resultsLi = results.getElementsByTag("li").firstOrNull()

					if (resultsLi == null) {
						context.reply(
								locale["$LOCALE_PREFIX.wordNotFound"],
								Constants.ERROR
						)
						return@executesDiscord
					}

					val linkElement = resultsLi.getElementsByClass("_sugg").first()
					val link = linkElement.attr("href")

					val httpRequest2 = HttpRequest.get("https://www.dicio.com.br$link")
							.userAgent(Constants.USER_AGENT)
					val response2 = httpRequest2.body()

					if (httpRequest2.code() == 404) {
						context.reply(
								locale["$LOCALE_PREFIX.wordNotFound"],
								Constants.ERROR
						)
						return@executesDiscord
					}

					jsoup = Jsoup.parse(response2)
				}

				if (jsoup.select("p[itemprop = description]").isEmpty() || jsoup.select("p[itemprop = description]")[0].text().startsWith("Ainda não temos o significado de")) {
					context.reply(
							LorittaReply(
									locale["$LOCALE_PREFIX.wordNotFound"],
									Constants.ERROR
							)
					)
					return@executesDiscord
				}

				val description = jsoup.select("p[itemprop = description]")[0]

				val type = description.getElementsByTag("span")[0]
				val what = description.getElementsByTag("span").getOrNull(1)
				val etim = if (description.getElementsByClass("etim").size > 0) description.getElementsByClass("etim").text() else ""
				val phrase = if (jsoup.getElementsByClass("frase").isNotEmpty()) {
					jsoup.getElementsByClass("frase")[0]
				} else {
					null
				}

				val embed = EmbedBuilder()
				embed.setColor(Color(25, 89, 132))
				embed.setFooter(etim, null)

				embed.setTitle("📙 ${locale["$LOCALE_PREFIX.meaningOf", context.args[0]]}")
				embed.setDescription("*${type.text()}*")
				if (what != null)
					embed.appendDescription("\n\n**${what.text()}**")

				if (jsoup.getElementsByClass("sinonimos").size > 0) {
					val synonym = jsoup.getElementsByClass("sinonimos")[0]

					embed.addField("🙂 ${locale["$LOCALE_PREFIX.synonym"]}", synonym.text(), false)
				}
				if (jsoup.getElementsByClass("sinonimos").size > 1) {
					val antonyms = jsoup.getElementsByClass("sinonimos")[1]

					embed.addField("🙁 ${locale["$LOCALE_PREFIX.antonyms"]}", antonyms.text(), false)
				}

				if (phrase != null) {
					embed.addField("🖋 ${locale["$LOCALE_PREFIX.phrase"]}", phrase.text(), false)
				}

				context.sendMessage(context.getUserMention(true), embed.build())
			} else {
				explain()
			}
		}
	}
}