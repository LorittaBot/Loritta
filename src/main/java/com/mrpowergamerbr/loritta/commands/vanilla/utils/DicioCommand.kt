package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.dv8tion.jda.core.EmbedBuilder
import org.jsoup.Jsoup
import java.awt.Color
import java.net.URLEncoder


class DicioCommand : CommandBase() {
	override fun getLabel(): String {
		return "dicio"
	}

	override fun getUsage(): String {
		return "palavra"
	}

	override fun getAliases(): List<String> {
		return listOf("dicionário", "definir")
	}

	override fun getDescription(): String {
		return "Procure o significado de uma palavra no dicionário!"
	}

	override fun getExample(): List<String> {
		return listOf("sonho");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS;
	}

	override fun run(context: CommandContext) {
		if (context.args.size == 1) {
			val palavra = URLEncoder.encode(context.args[0], "UTF-8");
			val httpRequest = HttpRequest.get("https://www.dicio.com.br/pesquisa.php?q=$palavra")
					.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0")
			val response = httpRequest.body();
			if (httpRequest.code() == 404) {
				context.sendMessage(context.getAsMention(true) + "Palavra não encontrada no meu dicionário!");
				return;
			}
			val jsoup = Jsoup.parse(response);

			// Se a página não possui uma descrição ou se ela possui uma descrição mas começa com "Ainda não temos o significado de", então é uma palavra inexistente!
			if (jsoup.select("p[itemprop = description]").isEmpty() || jsoup.select("p[itemprop = description]")[0].text().startsWith("Ainda não temos o significado de")) {
				context.sendMessage(context.getAsMention(true) + "Palavra não encontrada no meu dicionário!");
				return;
			}

			val description = jsoup.select("p[itemprop = description]")[0];

			val type = description.getElementsByTag("span")[0]
			val what = description.getElementsByTag("span")[1]
			val etim = if (description.getElementsByClass("etim").size > 0) description.getElementsByClass("etim").text() else "";
			var frase = if (jsoup.getElementsByClass("frase").isNotEmpty()) {
				jsoup.getElementsByClass("frase")[0]
			} else {
				null
			}

			val embed = EmbedBuilder();
			embed.setColor(Color(25, 89, 132))
			embed.setFooter(etim, null);

			embed.setTitle("📙 Significado de ${context.args[0]}")
			embed.setDescription("*${type.text()}*\n\n**${what.text()}**");

			if (jsoup.getElementsByClass("sinonimos").size > 0) {
				var sinonimos = jsoup.getElementsByClass("sinonimos")[0];

				embed.addField("🙂 Sinônimos", sinonimos.text(), false);
			}
			if (jsoup.getElementsByClass("sinonimos").size > 1) {
				var antonimos = jsoup.getElementsByClass("sinonimos")[1];

				embed.addField("🙁 Antônimos", antonimos.text(), false);
			}

			if (frase != null) {
				embed.addField("🖋 Frase", frase.text(), false);
			}

			context.sendMessage(context.getAsMention(true), embed.build());

		} else {
			this.explain(context);
		}
	}
}