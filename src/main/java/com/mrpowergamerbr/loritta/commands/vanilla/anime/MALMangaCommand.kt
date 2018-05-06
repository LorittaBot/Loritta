package com.mrpowergamerbr.loritta.commands.vanilla.anime

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.encodeToUrl
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.dv8tion.jda.core.EmbedBuilder
import org.apache.commons.lang3.StringEscapeUtils
import org.json.XML
import java.awt.Color

class MALMangaCommand : AbstractCommand("malmanga", category = CommandCategory.ANIME) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["MALMANGA_Description"]
	}

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf("query" to "Mangá que você deseja procurar")
	}

	override fun getExtendedExamples(): Map<String, String> {
		return mapOf(
				"Dragon Ball" to "Procura por \"Dragon Ball\" no MyAnimeList"
		)
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val query = context.args.joinToString(" ")
			val websiteResponse = HttpRequest.get("https://myanimelist.net/api/manga/search.xml?q=${query.encodeToUrl()}")
					.header("Authorization", "Basic ${Loritta.config.myAnimeListAuth}")
					.body()

			if (websiteResponse.isEmpty()) {
				// nothing found
				context.reply(
						LoriReply(
								message = locale["YOUTUBE_COULDNT_FIND", query],
								prefix = Constants.ERROR
						)
				)
				return
			}

			val xmlJSONObj = XML.toJSONObject(websiteResponse)

			val jsonPrettyPrintString = xmlJSONObj.toString(4)

			val obj = jsonParser.parse(jsonPrettyPrintString)["manga"]["entry"]


			if (!obj.isJsonArray) {
				val embed = displayAnime(obj.obj, locale)

				context.sendMessage(context.getAsMention(true), embed.build())
			} else {
				val animes = obj.array.map { it.obj }

				var idx = 0
				val embed = EmbedBuilder().apply {
					setColor(Color(255, 132, 188))
					setTitle(locale["YOUTUBE_RESULTS_FOR", query])

					var description = ""

					for ((index, anime) in animes.withIndex()) {
						if (index >= 9)
							break

						idx = index

						val title = anime["title"].string
						description += "${Constants.INDEXES[index]} [$title](https://myanimelist.net/manga/${anime["id"].string})\n"
					}
					setDescription(description)
				}

				val message = context.sendMessage(context.getAsMention(true), embed.build())

				message.onReactionAddByAuthor(context) {
					val index = Constants.INDEXES.indexOf(it.reactionEmote.name)

					if (index != -1) {
						val anime = animes.getOrNull(index) ?: return@onReactionAddByAuthor

						message.editMessage(displayAnime(anime, locale).build()).complete()

						message.clearReactions().queue()
					}
				}

				for ((index, emoji) in Constants.INDEXES.withIndex()) {
					if (index > idx)
						break
					message.addReaction(emoji).queue()
				}
			}
		} else {
			context.explain()
		}
	}

	fun displayAnime(obj: JsonObject, locale: BaseLocale): EmbedBuilder {
		println(obj)
		return EmbedBuilder().apply {
			setColor(Color(255, 132, 188))
			val title = obj["title"].string
			val volumes = obj["volumes"].string

			fun toDiscord(text: String): String {
				return StringEscapeUtils.unescapeHtml4(text)
						.replace("<br />", "\n")
						.replace("[i]", "*")
						.replace("[/i]", "*")
						.replace("[b]", "**")
						.replace("[/b]", "**")
			}

			setTitle("\uD83D\uDCFA $title", "https://myanimelist.net/manga/${obj["id"].string}")
			setThumbnail(obj["image"].string)
			val english = obj["english"].nullString
			var synonyms = obj["synonyms"].nullString
			var synopsis = toDiscord(obj["synopsis"].string)
			var startDate = obj["start_date"].string
			var endDate = obj["end_date"].string

			if (synonyms != null && synonyms.isNotEmpty()) {
				synopsis = "\uD83D\uDCDD **${locale["MALANIME_Synonyms"]} ** `${toDiscord(synonyms)}`\n" + synopsis
			}

			if (english != null && english.isNotEmpty() && title != english) {
				synopsis = "\uD83D\uDCDD **${locale["MALANIME_AlternateTitle"]}: ** `${toDiscord(english)}`\n" + synopsis
			}

			setDescription(synopsis)

			addField("\uD83E\uDD14 ${locale["MALANIME_Status"]}", obj["status"].string, true)
			if (volumes != "0")
				addField("\uD83D\uDCDA ${locale["MALMANGA_Volumes"]}", volumes, true)
			addField("\uD83D\uDC40 ${locale["MALANIME_Type"]}", obj["type"].string, true)
			addField("\uD83D\uDC4D ${locale["MALANIME_Score"]}", "${obj["score"].double}/10", true)
			addField("⛓ Link", "https://myanimelist.net/anime/${obj["id"].string}", true)

			// 0000-00-00 = ainda não encerrado
			if (endDate != "0000-00-00") {
				setFooter("⏰ $startDate ${locale["MALANIME_To"]} $endDate", null)
			} else {
				setFooter("⏰ ${locale["MALANIME_InProductionSince"]} $startDate", null)
			}
		}
	}
}