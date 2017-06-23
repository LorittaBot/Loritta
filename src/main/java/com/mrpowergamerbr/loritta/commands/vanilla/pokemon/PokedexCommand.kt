package com.mrpowergamerbr.loritta.commands.vanilla.pokemon

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.dv8tion.jda.core.EmbedBuilder
import org.jsoup.Jsoup
import java.util.*

class PokedexCommand : CommandBase() {
    override fun getLabel(): String {
        return "pokedex";
    }

    override fun getDescription(): String {
        return "Pesquisa informações sobre um Pokémon"
    }

    override fun getCategory(): CommandCategory {
        return CommandCategory.POKEMON;
    }

    override fun getExample(): List<String> {
        return Arrays.asList("Pikachu")
    }

	override fun getAliases(): List<String> {
		return listOf("pokédex");
	}

    override fun getUsage(): String {
        return "pokémon"
    }

    override fun run(context: CommandContext?) {
        if (context!!.args.size == 1) {
            // Argumento 1: Pokémon (ID ou Nome)
			var http = HttpRequest.get("https://veekun.com/dex/pokemon/${context.args[0].toLowerCase()}").userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0");
			if (http.notFound()) {
				context.sendMessage(context.getAsMention(true) + "Pokémon não encontrado!");
				return;
			}
			var response = http.body();
			var jsoup = Jsoup.parse(response);

			var name = jsoup.getElementById("dex-page-name").html();
			var description = jsoup.getElementById("dex-pokemon-genus").html();
			var spriteDiv = jsoup.getElementById("dex-pokemon-portrait-sprite");
			var sprite = "https://veekun.com" + spriteDiv.getElementsByTag("img")[0].attr("src");
			var abilities = jsoup.getElementsByClass("pokemon-abilities");
			var dexTypes = jsoup.getElementById("dex-page-types").getElementsByTag("img");
			var dexColumn = jsoup.getElementsByClass("dex-column");
			var chain = jsoup.getElementsByClass("dex-evolution-chain")[0];
			var evolutions = chain.getElementsByTag("td");

			// var pokeInfoName = dexColumn[0].getElementsByTag("dl"); // Pokédex Numbers
			var pokeInfoValue = dexColumn[0].getElementsByTag("dd"); // Pokédex Numbers
			var breedingInfo = dexColumn[1]; // Breeding
			var breedingInfoValue = dexColumn[1].getElementsByTag("dd"); // Breeding
			var trainingInfo = dexColumn[2]; // Training
			var trainingInfoValue = dexColumn[2].getElementsByTag("dd") // Training

			var embed = EmbedBuilder();

			embed.setTitle(name, "https://veekun.com/dex/pokemon/${context.args[0].toLowerCase()}");
			embed.setDescription(description);
			embed.setThumbnail(sprite);

			var strAbilities = "";
			var strDexTypes = dexTypes.joinToString(separator = ", ", transform = { it.attr("alt") });

			embed.addField("Tipos", strDexTypes, true);

			embed.addField("Adicionado na Geração", pokeInfoValue[0].getElementsByTag("img")[0].attr("alt"), true)

			embed.addField("Número na Pokédex", pokeInfoValue[1].text(), true)

			for (el in abilities) {
				// title
				var title = el.getElementsByTag("dt")[0].getElementsByTag("a").text()
				var description = el.getElementsByTag("dd")[0].getElementsByTag("p").text()
				strAbilities += "**$title** - $description\n";
			}

			embed.addField("Habilidades", strAbilities, true);

			var strTraining = "**Base EXP:** ${trainingInfoValue[0].text()}" +
					"\n**Effort points:** ${trainingInfoValue[1].text()}" +
					"\n**Taxa de Captura:** ${trainingInfoValue[2].text()}" +
					"\n**Base happiness:** ${trainingInfoValue[3].text()}" +
					"\n**Taxa de crescimento:** ${trainingInfoValue[4].text()}";

			embed.addField("Treinamento", strTraining, true)

			var strEvolutions = "";

			for (el in evolutions) {
				if (el.attr("rowspan") == "1") {
					if (el.getElementsByClass("dex-evolution-chain-pokemon")[0].text() == name) {
						strEvolutions += "**";
					}
					strEvolutions += el.getElementsByClass("dex-evolution-chain-pokemon")[0].text();
					if (el.getElementsByClass("dex-evolution-chain-pokemon")[0].text() == name) {
						strEvolutions += "**";
					}
					var evolMethod = el.getElementsByClass("dex-evolution-chain-method");
					if (evolMethod.isNotEmpty()) {
						strEvolutions += " | " + evolMethod[0].text()
					}
					strEvolutions += "\n";
				}
			}

			embed.addField("Evoluções", strEvolutions, true)

			context.sendMessage(embed.build())

        } else {
            this.explain(context);
        }
    }
}