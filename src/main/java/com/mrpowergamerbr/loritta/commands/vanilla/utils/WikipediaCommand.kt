package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.nullObj
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import org.apache.commons.lang3.StringUtils
import java.awt.Color
import java.net.URLEncoder
import java.util.*

class WikipediaCommand : AbstractCommand("wikipedia", category = CommandCategory.UTILS) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["WIKIPEDIA_DESCRIPTION"]
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

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			var languageId = when (context.config.localeId) {
				"default" -> "pt"
				"pt-pt" -> "pt"
				"pt-funk" -> "pt"
				else -> "en"
			}

			val inputLanguageId = context.args[0]
			var hasValidLanguageId = false
			if (inputLanguageId.startsWith("[") && inputLanguageId.endsWith("]")) {
				languageId = inputLanguageId.substring(1, inputLanguageId.length - 1)
				hasValidLanguageId = true
			}
			try {
				val query = StringUtils.join(context.args, " ", if (hasValidLanguageId) 1 else 0, context.args.size)
				val wikipediaResponse = HttpRequest.get("https://" + languageId + ".wikipedia.org/w/api.php?format=json&action=query&prop=extracts&redirects=1&exintro=&explaintext=&titles=" + URLEncoder.encode(query, "UTF-8")).body()
				val wikipedia = jsonParser.parse(wikipediaResponse).asJsonObject // Base
				val wikiQuery = wikipedia.getAsJsonObject("query") // Query
				val wikiPages = wikiQuery.getAsJsonObject("pages") // Páginas
				val entryWikiContent = wikiPages.entrySet().iterator().next() // Conteúdo

				if (entryWikiContent.key == "-1") { // -1 = Nenhuma página encontrada
					context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["WIKIPEDIA_COULDNT_FIND", query])
				} else {
					// Se não é -1, então é algo que existe! Yay!
					val pageTitle = entryWikiContent.value.asJsonObject.get("title").asString
					val pageExtract = entryWikiContent.value.asJsonObject.get("extract").asString

					val embed = EmbedBuilder()
							.setTitle("<:wikipedia:400981794666840084> $pageTitle", null)
							.setColor(Color.BLACK)
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

fun main(args: Array<String>) {
	val query = "Shantae"

	val body = HttpRequest.get("https://en.wikipedia.org/w/api.php?action=query&generator=search&format=json&gsrwhat=text&gsrlimit=2&prop=categories|extracts|pageimages&exintro=&explaintext=&cllimit=max&piprop=original&gsrsearch=${URLEncoder.encode(query, "UTF-8")}")
			.body()

	val wikipedia = jsonParser.parse(body).obj
	val queryResponse = wikipedia["query"].nullObj

	if (queryResponse == null) {
		// Nada encontrado
		return
	}

	val pages = queryResponse["pages"].obj

	var pageResponse: JsonObject? = null

	for ((s, jsonElement) in pages.entrySet()) {
		val categories = jsonElement["categories"].array
		val isDisambiguation = categories.filter { it["title"].string == "Category:Disambiguation pages" }.isNotEmpty()

		if (!isDisambiguation) {
			pageResponse = jsonElement.obj
		}
	}

	if (pageResponse == null) {
		// Nada encontrado
		return
	}

	val originalImage = pageResponse["original"].nullObj
	val sourceImage = originalImage?.get("source").nullString
}