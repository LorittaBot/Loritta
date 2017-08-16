package com.mrpowergamerbr.loritta.commands.vanilla.utils;

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.f
import com.mrpowergamerbr.loritta.utils.getOrCreateWebhook
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.temmiewebhook.DiscordEmbed
import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import com.mrpowergamerbr.temmiewebhook.embed.ThumbnailEmbed
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.util.*

class ReceitasCommand : CommandBase() {
	override fun getLabel(): String {
		return "anamaria";
	}

	override fun getAliases(): MutableList<String> {
		return Arrays.asList("receitas");
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.get("RECEITAS_DESCRIPTION")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS
	}

	override fun hasCommandFeedback(): Boolean {
		return false;
	}

	override fun getUsage(): String {
		return "<texto>";
	}

	override fun getExtendedExamples(): Map<String, String> {
		return mapOf("bolo" to "Procura \"bolo\" no livro de receitas da Ana Maria Braga™")
	}

	override fun run(context: CommandContext) {
		if (context.args.size > 0) {
			val query = context.args.joinToString(" ");

			val jsoup = Jsoup.connect("http://anamariabraga.globo.com/Publicacao/Filtrar?term=" + URLEncoder.encode(query, "UTF-8") + "&pagina=1&quantidadePorPagina=30&tipoPublicacao=&idCategoria=&videoOnly=false").get()

			var classes = jsoup.getElementsByClass("col-lg-4");

			var finalMessage: DiscordMessage? = null

			for (c in classes) {
				try {
					var links = c.select("a[href]");
					var comida = c.select("h3");
					var reason = c.getElementsByClass("text-uppercase")[0].text()

					var img = c.getElementsByClass("hover-zoom")[0].attr("abs:data-image")

					var embed = DiscordEmbed.builder()
							.thumbnail(ThumbnailEmbed(img, null, 700, 700))
							.title("\uD83E\uDD58 ${comida.html()}")
							.url(links[0].attr("abs:href"))
							.description(context.locale.RECEITAS_INFO.f(reason))
							.color(744725)
							.build()

					var message = DiscordMessage.builder()
							.username("Louro José")
							.embed(embed)
							.avatarUrl("http://s2.glbimg.com/bcMLrkFsNfZn_ySj2P1IZCwjSLQ=/s.glbimg.com/et/pr/f/original/2014/03/05/louro.jpg")
							.content(context.getAsMention(true))
							.build();

					finalMessage = message;
				} catch (e: IndexOutOfBoundsException) {
				}
			}

			if (finalMessage != null) {
				context.sendMessage(getOrCreateWebhook(context.event.textChannel, "Louro José"), finalMessage);
			} else {
				var message = DiscordMessage.builder()
						.username("Louro José")
						.avatarUrl("http://s2.glbimg.com/bcMLrkFsNfZn_ySj2P1IZCwjSLQ=/s.glbimg.com/et/pr/f/original/2014/03/05/louro.jpg")
						.content(context.getAsMention(true) + context.locale.RECEITAS_COULDNT_FIND.f(query))
						.build();

				context.sendMessage(getOrCreateWebhook(context.event.textChannel, "Louro José"), message);
			}
		} else {
			this.explain(context);
		}
	}
}