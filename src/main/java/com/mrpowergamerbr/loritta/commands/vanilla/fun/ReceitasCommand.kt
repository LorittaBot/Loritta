package com.mrpowergamerbr.loritta.commands.vanilla.`fun`;

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
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

	override fun getDescription(): String {
		return "Procure receitas delíciosas da Ana Maria Braga™!";
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN;
	}

	override fun hasCommandFeedback(): Boolean {
		return false;
	}

	override fun getUsage(): String {
		return "<texto>";
	}

	override fun run(context: CommandContext) {
		if (context.args.size > 0) {
			val query = context.args.joinToString(" ");

			val jsoup = Jsoup.connect("http://anamariabraga.globo.com/Publicacao/Filtrar?term=" + URLEncoder.encode(query, "UTF-8") + "&pagina=1&quantidadePorPagina=30").get()

			var classes = jsoup.getElementsByClass("col-lg-4");

			if (classes.size > 0) {
				var c = classes[0];
				var links = c.select("a[href]");
				var comida = c.select("h3");
				var reason = c.getElementsByClass("text-uppercase")[0];
				var img = c.getElementsByClass("hover-zoom")[0]

				var embed = DiscordEmbed.builder()
						.thumbnail(ThumbnailEmbed(img.attr("abs:data-image"), null, 700, 700))
						.title(comida.html())
						.description("Um delícioso " + reason.html() + " para a sua família!\n\n[Link](" + links[0].attr("abs:href") + ")")
						.build()

				var message = DiscordMessage.builder()
						.username("Louro José")
						.embed(embed)
						.avatarUrl("http://s2.glbimg.com/bcMLrkFsNfZn_ySj2P1IZCwjSLQ=/s.glbimg.com/et/pr/f/original/2014/03/05/louro.jpg")
						.content(" ")
						.build();
				context.sendMessage(Loritta.getOrCreateWebhook(context.event.textChannel, "Louro José"), message);
			} else {
				var message = DiscordMessage.builder()
						.username("Louro José")
						.avatarUrl("http://s2.glbimg.com/bcMLrkFsNfZn_ySj2P1IZCwjSLQ=/s.glbimg.com/et/pr/f/original/2014/03/05/louro.jpg")
						.content("Não encontrei nada relacionado a \"${query}\" no livro de receitas da Ana Maria Braga!")
						.build();

				context.sendMessage(Loritta.getOrCreateWebhook(context.event.textChannel, "Louro José"), message);
			}
		} else {
			this.explain(context);
		}
	}
}