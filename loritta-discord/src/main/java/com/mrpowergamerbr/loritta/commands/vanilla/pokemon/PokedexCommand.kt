package com.mrpowergamerbr.loritta.commands.vanilla.pokemon

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.encodeToUrl
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.commands.CommandCategory
import org.jsoup.Jsoup
import java.awt.Color
import java.util.*

class PokedexCommand : AbstractCommand("pokedex", listOf("pokédex"), CommandCategory.POKEMON) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.pokedex.description")

    override fun getExamples(): List<String> {
        return Arrays.asList("Pikachu")
    }

	// TODO: Fix Usage

    override suspend fun run(context: CommandContext,locale: BaseLocale) {
        if (context.args.size == 1) {
            // Argumento 1: Pokémon (ID ou Nome)
			var http = HttpRequest.get("https://veekun.com/dex/pokemon/${context.args[0].toLowerCase().encodeToUrl()}").userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0")
	        if (http.notFound()) {
				context.sendMessage(context.getAsMention(true) + "Pokémon não encontrado!")
				return
			}
			var response = http.body()
	        var jsoup = Jsoup.parse(response)

	        var name = jsoup.getElementById("dex-page-name").html()
	        var description = jsoup.getElementById("dex-pokemon-genus")?.html() ?: ""
			var spriteDiv = jsoup.getElementById("dex-pokemon-portrait-sprite")
	        var sprite = "https://veekun.com" + spriteDiv.getElementsByTag("img")[0].attr("src")
	        var abilities = jsoup.getElementsByClass("pokemon-abilities")
	        var dexTypes = jsoup.getElementById("dex-page-types").getElementsByTag("img")
	        var dexColumn = jsoup.getElementsByClass("dex-column")
	        var chain = jsoup.getElementsByClass("dex-evolution-chain")[0]
	        var evolutions = chain.getElementsByTag("td")

	        // var pokeInfoName = dexColumn[0].getElementsByTag("dl"); // Pokédex Numbers
			var pokeInfoValue = dexColumn[0].getElementsByTag("dd") // Pokédex Numbers
			var breedingInfo = dexColumn[1] // Breeding
			var breedingInfoValue = dexColumn[1].getElementsByTag("dd") // Breeding
			var trainingInfo = dexColumn[2] // Training
			var trainingInfoValue = dexColumn[2].getElementsByTag("dd") // Training

			var embed = EmbedBuilder()

	        embed.apply {
				setTitle("<:pokeball:412575443024216066> $name", "https://veekun.com/dex/pokemon/${context.args[0].toLowerCase()}")
				setDescription(description)
				setThumbnail(sprite)
				setColor(Color(255, 28, 28))
			}

			var strAbilities = ""
	        var strDexTypes = dexTypes.joinToString(separator = ", ", transform = { it.attr("alt") })

	        embed.addField(locale["commands.command.pokedex.types"], strDexTypes, true)

	        embed.addField(locale["commands.command.pokedex.addedInGen"], pokeInfoValue[0].getElementsByTag("img")[0].attr("alt"), true)

			embed.addField(locale["commands.command.pokedex.number"], pokeInfoValue[1].text(), true)

			for (el in abilities) {
				// title
				var title = el.getElementsByTag("dt")[0].getElementsByTag("a").text()
				var description = el.getElementsByTag("dd")[0].getElementsByTag("p").text()
				strAbilities += "**$title** - $description\n"
			}

			embed.addField(locale["commands.command.pokedex.abilities"], strAbilities, true)

	        var strTraining = "**${context.locale["commands.command.pokedex.baseExp"]}:** ${trainingInfoValue[0].text()}" +
					"\n**${locale["commands.command.pokedex.effortPoints"]}:** ${trainingInfoValue[1].text()}" +
					"\n**${locale["commands.command.pokedex.captureRate"]}:** ${trainingInfoValue[2].text()}" +
					"\n**${locale["commands.command.pokedex.baseHappiness"]}:** ${trainingInfoValue[3].text()}" +
					"\n**${locale["commands.command.pokedex.growthRate"]}:** ${trainingInfoValue[4].text()}"

	        embed.addField("${locale["commands.command.pokedex.training"]}", strTraining, true)

			var strEvolutions = ""

	        for (el in evolutions) {
				if (el.attr("rowspan") == "1") {
					if (el.getElementsByClass("dex-evolution-chain-pokemon")[0].text() == name) {
						strEvolutions += "**"
					}
					strEvolutions += el.getElementsByClass("dex-evolution-chain-pokemon")[0].text()
					if (el.getElementsByClass("dex-evolution-chain-pokemon")[0].text() == name) {
						strEvolutions += "**"
					}
					var evolMethod = el.getElementsByClass("dex-evolution-chain-method")
					if (evolMethod.isNotEmpty()) {
						strEvolutions += " **|** " + evolMethod[0].text()
					}
					strEvolutions += "\n"
				}
			}

			embed.addField("${locale["commands.command.pokedex.evolutions"]}", strEvolutions, true)

			context.sendMessage(embed.build())

        } else {
            this.explain(context)
        }
    }
}