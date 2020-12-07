package net.perfectdreams.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.utils.Constants
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import org.apache.commons.lang3.StringUtils
import java.awt.Color
import java.net.URLEncoder

class WikipediaCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("wikipedia", "wiki"), CommandCategory.UTILS) {
	companion object {
		private const val LOCALE_PREFIX = "commands.utils.wikipedia"
	}

	override fun command() = create {
		localizedDescription("$LOCALE_PREFIX.description")
		localizedExamples("$LOCALE_PREFIX.examples")

		executesDiscord {
			val context = this

			if (context.args.isNotEmpty()) {
				var languageId = when (context.serverConfig.localeId) {
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
					val wikipedia = JsonParser.parseString(wikipediaResponse).asJsonObject
					val wikiQuery = wikipedia.getAsJsonObject("query")
					val wikiPages = wikiQuery.getAsJsonObject("pages")
					val entryWikiContent = wikiPages.entrySet().iterator().next()

					if (entryWikiContent.key == "-1") {
						context.reply(
								LorittaReply(
										locale["$LOCALE_PREFIX.couldntFind", query],
										Constants.ERROR
								)
						)
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
					context.reply(
							"**Deu ruim!**"
					)
				}
			} else {
				context.explain()
			}
		}
	}
}