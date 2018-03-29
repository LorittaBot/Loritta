package com.mrpowergamerbr.loritta.commands.vanilla.utils;

import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.f
import com.mrpowergamerbr.loritta.utils.getOrCreateWebhook
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.temmiewebhook.DiscordEmbed
import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import com.mrpowergamerbr.temmiewebhook.embed.FooterEmbed
import com.mrpowergamerbr.temmiewebhook.embed.ThumbnailEmbed
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.util.*

class ReceitasCommand : AbstractCommand("receitas", listOf("anamaria"), CommandCategory.UTILS) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["RECEITAS_DESCRIPTION"]
	}

	override fun hasCommandFeedback(): Boolean {
		return false
	}

	override fun getUsage(): String {
		return "<texto>";
	}

	override fun getExtendedExamples(): Map<String, String> {
		return mapOf("bolo" to "Procura \"bolo\" no livro de receitas da Ana Maria Braga™")
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val query = context.args.joinToString(" ");

			val jsoup = Jsoup.connect("http://anamariabraga.globo.com/Publicacao/Filtrar?term=" + URLEncoder.encode(query, "UTF-8") + "&pagina=1&quantidadePorPagina=30&tipoPublicacao=&idCategoria=10&videoOnly=false").get()

			var finalMessage: DiscordMessage? = null
			val classes = jsoup.getElementsByClass("col-lg-4")
			if (classes.isNotEmpty()) {
				var klass = classes[RANDOM.nextInt(classes.size)]

				val link = klass.select("a[href]")[0]
				val comida = link.attr("title")
				val reason = link.getElementsByClass("text-uppercase")[0].text()

				val img = link.getElementsByClass("hover-zoom")[0].attr("abs:data-image")

				val recipePage = Jsoup.connect(link.attr("abs:href")).get()

				var avatarUrl = recipePage.getElementsByClass("info-receita")[0].getElementsByClass("img-avatar").attr("src")
				val creator = recipePage.getElementsByClass("f-w-500")[1].text()
				val date = recipePage.getElementsByClass("f-w-500")[0].text()

				val detalhes = recipePage.getElementsByClass("detalhes-receita")[0]
				val porcoes = detalhes.getElementsByClass("f-h-500")[0].text()
				val time = detalhes.getElementsByClass("f-h-500")[1].text()
				val difficulty = detalhes.getElementsByClass("f-h-500")[2].text()

				if (creator == "Por Ana Maria Braga") {
					avatarUrl = "https://media-exp2.licdn.com/mpr/mpr/shrinknp_200_200/AAEAAQAAAAAAAAOmAAAAJDNlYjU2YmIwLTA1ZjAtNDVkOC04ZTk3LWQ2MjUyYmM5NWRkYQ.jpg"
				}

				val embed = DiscordEmbed.builder()
						.thumbnail(ThumbnailEmbed(img, null, 700, 700))
						.title("\uD83E\uDD58 ${comida}")
						.url(link.attr("abs:href"))
						.description(locale["RECEITAS_INFO", reason] + "\n\n\uD83D\uDE0B **Porções:** $porcoes\n\uD83D\uDD52 **Tempo:** $time\n\uD83C\uDF7D **Dificuldade:** $difficulty")
						.color(744725)
						.footer(FooterEmbed("$creator | $reason", avatarUrl, null))
						.build()

				val message = DiscordMessage.builder()
						.username("Louro José")
						.embed(embed)
						.avatarUrl("http://s2.glbimg.com/bcMLrkFsNfZn_ySj2P1IZCwjSLQ=/s.glbimg.com/et/pr/f/original/2014/03/05/louro.jpg")
						.content(context.getAsMention(true))
						.build()

				finalMessage = message;
			}

			if (finalMessage != null) {
				context.sendMessage(getOrCreateWebhook(context.event.textChannel!!, "Louro José"), finalMessage);
			} else {
				var message = DiscordMessage.builder()
						.username("Louro José")
						.avatarUrl("http://s2.glbimg.com/bcMLrkFsNfZn_ySj2P1IZCwjSLQ=/s.glbimg.com/et/pr/f/original/2014/03/05/louro.jpg")
						.content(context.getAsMention(true) + context.locale["RECEITAS_COULDNT_FIND", query])
						.build();

				context.sendMessage(getOrCreateWebhook(context.event.textChannel!!, "Louro José"), message)
			}
		} else {
			this.explain(context);
		}
	}
}