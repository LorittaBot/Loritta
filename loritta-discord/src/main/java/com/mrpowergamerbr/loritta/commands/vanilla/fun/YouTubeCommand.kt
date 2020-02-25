package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.misc.YouTubeUtils
import com.mrpowergamerbr.loritta.utils.temmieyoutube.YouTubeItem
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.CommandCategory
import org.jsoup.parser.Parser
import java.awt.Color
import java.util.*

class YouTubeCommand : AbstractCommand("youtube", listOf("yt"), category = CommandCategory.FUN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["YOUTUBE_DESCRIPTION"]
	}

	override fun getExamples(): List<String> {
		return Arrays.asList("shantae tassel town")
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			val query = context.args.joinToString(" ")
			val items = YouTubeUtils.searchOnYouTube(query, "youtube#video", "youtube#channel")

			if (items.isNotEmpty()) {
				var format = ""
				val youtubeKey = loritta.config.youtube.apiKey
				for (i in 0 until Math.min(10, items.size)) {
					val item = items[i]
					if (item.id["kind"].string == "youtube#video") {
						val response = HttpRequest.get("https://www.googleapis.com/youtube/v3/videos?id=${item.id["videoId"].string}&part=contentDetails&key=${youtubeKey}").body()

						val parser = jsonParser
						val json = parser.parse(response).asJsonObject
						val strDuration = json["items"].array[0]["contentDetails"]["duration"].string
						val duration = java.time.Duration.parse(strDuration)
						val inSeconds = duration.get(java.time.temporal.ChronoUnit.SECONDS) // Nós não podemos pegar o tempo diretamente porque é "unsupported"
						val final = String.format("%02d:%02d", ((inSeconds / 60) % 60), (inSeconds % 60))
						format += "${Constants.INDEXES[i]} \uD83C\uDFA5 `[${final}]` **[${Parser.unescapeEntities(item.snippet.title, false)}](https://youtu.be/${item.id["videoId"].string})**\n"
					} else {
						format += "${Constants.INDEXES[i]} \uD83D\uDCFA **[${Parser.unescapeEntities(item.snippet.title, false)}](https://youtube.com/channel/${item.id["channelId"].string})**\n"
					}
					context.metadata.put(i.toString(), item)
				}

				val embed = EmbedBuilder()
				embed.setColor(Color(217, 66, 52))
				embed.setDescription(format)
				embed.setTitle("<:youtube:314349922885566475> ${context.legacyLocale["YOUTUBE_RESULTS_FOR", query]}")
				val mensagem = context.sendMessage(context.getAsMention(true), embed.build())

				mensagem.onReactionAddByAuthor(context) {
					val idx = Constants.INDEXES.indexOf(it.reactionEmote.name)

					// Caso seja uma reaçõa inválida ou que não tem no metadata, ignore!
					if (idx == -1 || !context.metadata.containsKey(idx.toString()))
						return@onReactionAddByAuthor

					val item: YouTubeItem = context.metadata[idx.toString()] as YouTubeItem

					// Remover todos os reactions
					mensagem.clearReactions().queue {
						if (item.id["kind"].string == "youtube#video") { // Se é um vídeo...
							// Parece completamente idiota isto, mas não tem jeito de esconder uma embed já enviada :(
							// Infelizmente é necessário reenviar a mensagem
							mensagem.delete().queue()

							GlobalScope.launch(loritta.coroutineDispatcher) {
								context.reply(
										LoriReply(
												"https://youtu.be/${item.id["videoId"].string}",
												"\uD83D\uDCFA"
										)
								)
								context.metadata.put("currentItem", item)
							}
						} else {
							val channelResponse = HttpRequest.get("https://www.googleapis.com/youtube/v3/channels?part=snippet,statistics&id=${item.id["channelId"].string}&key=${loritta.config.youtube.apiKey}").body()

							val channelJson = jsonParser.parse(channelResponse).obj

							val embed = EmbedBuilder()

							val entry = channelJson["items"][0]

							val title = entry["snippet"]["title"].string
							val description = entry["snippet"]["description"].string
							val channelIcon = entry["snippet"]["thumbnails"]["high"]["url"].string
							val viewCount = entry["statistics"]["viewCount"].string
							val subscriberCount = entry["statistics"]["subscriberCount"].string
							val videoCount = entry["statistics"]["videoCount"].string

							embed.setTitle(title, "https://youtube.com/channel/${item.snippet.channelId}")
							embed.setThumbnail(channelIcon)
							embed.setDescription(description)
							embed.setColor(Color(217, 66, 52))
							embed.addField("\uD83D\uDCFA ${context.legacyLocale["MUSICINFO_VIEWS"]}", viewCount, true)
							embed.addField("\uD83D\uDC3E ${context.legacyLocale["YOUTUBE_Subscribers"]}", subscriberCount, true)
							embed.addField("\uD83C\uDFA5 ${context.legacyLocale["YOUTUBE_Videos"]}", videoCount, true)

							embed.addField("⛓ Link", "https://youtube.com/channel/${item.snippet.channelId}", true)
							// Criar novo embed!
							mensagem.editMessage(embed.build()).queue()
						}
					}
				}

				// Adicionar os reactions
				for (i in 0 until Math.min(10, items.size)) {
					mensagem.addReaction(Constants.INDEXES[i]).queue()
				}
			} else {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["YOUTUBE_COULDNT_FIND", query])
			}
		} else {
			context.explain()
		}
	}
}