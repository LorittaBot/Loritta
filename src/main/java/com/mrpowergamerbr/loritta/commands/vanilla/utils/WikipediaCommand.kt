package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.dv8tion.jda.core.EmbedBuilder
import org.apache.commons.lang3.StringUtils
import java.awt.Color
import java.net.URLEncoder
import java.util.*

class WikipediaCommand : CommandBase() {
	override fun getLabel(): String {
		return "wikipedia"
	}

	override fun getDescription(): String {
		return "Mostra uma versão resumida de uma página do Wikipedia"
	}

	override fun getUsage(): String {
		return "[linguagem] conteúdo"
	}

	override fun getExample(): List<String> {
		return Arrays.asList("Minecraft", "[en] Shantae")
	}

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf("linguagem" to "*(Opcional)* Código de linguagem para procurar no Wikipédia, entre [], por padrão ele irá procurar na Wikipedia de Portugal [pt]",
				"conteúdo" to "O que você deseja procurar no Wikipédia")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.MISC
	}

	override fun run(context: CommandContext) {
		if (context.args.size >= 1) {
			var languageId = "pt"
			val inputLanguageId = context.args[0]
			var hasValidLanguageId = false
			if (inputLanguageId.startsWith("[") && inputLanguageId.endsWith("]")) {
				languageId = inputLanguageId.substring(1, inputLanguageId.length - 1)
				hasValidLanguageId = true
			}
			try {
				val query = StringUtils.join(context.args, " ", if (hasValidLanguageId) 1 else 0, context.args.size)
				val wikipediaResponse = HttpRequest.get("https://" + languageId + ".wikipedia.org/w/api.php?format=json&action=query&prop=extracts&redirects=1&exintro=&explaintext=&titles=" + URLEncoder.encode(query, "UTF-8")).body()
				val wikipedia = JsonParser().parse(wikipediaResponse).asJsonObject // Base
				val wikiQuery = wikipedia.getAsJsonObject("query") // Query
				val wikiPages = wikiQuery.getAsJsonObject("pages") // Páginas
				val entryWikiContent = wikiPages.entrySet().iterator().next() // Conteúdo

				if (entryWikiContent.key == "-1") { // -1 = Nenhuma página encontrada
					context.sendMessage(context.getAsMention(true) + "Não consegui encontrar nada relacionado á **" + query + "** 😞")
				} else {
					// Se não é -1, então é algo que existe! Yay!
					val pageTitle = entryWikiContent.value.asJsonObject.get("title").asString
					val pageExtract = entryWikiContent.value.asJsonObject.get("extract").asString

					val embed = EmbedBuilder()
							.setTitle(pageTitle, null)
							.setColor(Color.BLUE)
							.setDescription(if (pageExtract.length > 512) pageExtract.substring(0, 509) + "..." else pageExtract)

					context.sendMessage(embed.build()) // Envie a mensagem!
				}

			} catch (e: Exception) {
				e.printStackTrace()
				context.sendMessage(context.getAsMention(true) + "**Deu ruim!**")
			}
		} else {
			context.explain()
		}
	}
}