package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.commands.CommandCategory
import org.jsoup.Jsoup
import java.awt.Color
import java.net.URLEncoder


class DicioCommand : AbstractCommand("dicio", listOf("dicionário", "dicionario", "definir"), CommandCategory.UTILS) {
	// TODO: Fix Usage

	override fun getDescriptionKey() = LocaleKeyData("commands.command.dicio.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.dicio.examples")

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.size == 1) {
			val palavra = URLEncoder.encode(context.args[0], "UTF-8")
			val httpRequest = HttpRequest.get("https://www.dicio.com.br/pesquisa.php?q=$palavra")
					.userAgent(Constants.USER_AGENT)
			val response = httpRequest.body()

			if (httpRequest.code() == 404) {
				context.reply(
						"Palavra não encontrada no meu dicionário!",
						Constants.ERROR
				)
				return
			}

			var jsoup = Jsoup.parse(response)

			// Ao usar pesquisa.php, ele pode retornar uma página de pesquisa ou uma página com a palavra, por isto iremos primeiro descobrir se estamos na página de pesquisa
			val resultadosClass = jsoup.getElementsByClass("resultados")
			val resultados = resultadosClass.firstOrNull()

			if (resultados != null) {
				val resultadosLi = resultados.getElementsByTag("li").firstOrNull()

				if (resultadosLi == null) {
					context.reply(
							"Palavra não encontrada no meu dicionário!",
							Constants.ERROR
					)
					return
				}

				val linkElement = resultadosLi.getElementsByClass("_sugg").first()
				val link = linkElement.attr("href")

				val httpRequest2 = HttpRequest.get("https://www.dicio.com.br$link")
						.userAgent(Constants.USER_AGENT)
				val response2 = httpRequest2.body()

				if (httpRequest2.code() == 404) {
					context.reply(
							"Palavra não encontrada no meu dicionário!",
							Constants.ERROR
					)
					return
				}

				jsoup = Jsoup.parse(response2)
			}

			// Se a página não possui uma descrição ou se ela possui uma descrição mas começa com "Ainda não temos o significado de", então é uma palavra inexistente!
			if (jsoup.select("p[itemprop = description]").isEmpty() || jsoup.select("p[itemprop = description]")[0].text().startsWith("Ainda não temos o significado de")) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + "Palavra não encontrada no meu dicionário!")
				return
			}

			val description = jsoup.select("p[itemprop = description]")[0]

			val type = description.getElementsByTag("span")[0]
			val word = jsoup.select("h1[itemprop = name]")
			val what = description.getElementsByTag("span").getOrNull(1)
			val etim = if (description.getElementsByClass("etim").size > 0) description.getElementsByClass("etim").text() else ""
			val frase = if (jsoup.getElementsByClass("frase").isNotEmpty()) {
				jsoup.getElementsByClass("frase")[0]
			} else {
				null
			}

			val embed = EmbedBuilder()
			embed.setColor(Color(25, 89, 132))
			embed.setFooter(etim, null)

			embed.setTitle("📙 Significado de ${word.text()}")
			embed.setDescription("*${type.text()}*")
			if (what != null)
				embed.appendDescription("\n\n**${what.text()}**")

			if (jsoup.getElementsByClass("sinonimos").size > 0) {
				val sinonimos = jsoup.getElementsByClass("sinonimos")[0]

				embed.addField("🙂 Sinônimos", sinonimos.text(), false)
			}
			if (jsoup.getElementsByClass("sinonimos").size > 1) {
				val antonimos = jsoup.getElementsByClass("sinonimos")[1]

				embed.addField("🙁 Antônimos", antonimos.text(), false)
			}

			if (frase != null) {
				embed.addField("🖋 Frase", frase.text(), false)
			}

			context.sendMessage(context.getAsMention(true), embed.build())
		} else {
			this.explain(context)
		}
	}
}
