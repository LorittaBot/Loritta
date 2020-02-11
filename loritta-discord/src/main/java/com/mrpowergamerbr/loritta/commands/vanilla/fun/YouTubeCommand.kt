package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.misc.YouTubeUtils
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import com.mrpowergamerbr.loritta.utils.temmieyoutube.YouTubeItem
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

					var item: YouTubeItem = context.metadata[idx.toString()] as YouTubeItem

					// Remover todos os reactions
					mensagem.clearReactions().queue {
						if (item.id["kind"].string == "youtube#video") { // Se é um vídeo...
							val response = HttpRequest.get("https://www.googleapis.com/youtube/v3/videos?id=${item.id["videoId"].string}&part=snippet,statistics&key=${loritta.config.youtube.apiKey}").body()
							val parser = JsonParser()
							val json = parser.parse(response).asJsonObject
							val jsonItem = json["items"][0]
							val snippet = jsonItem["snippet"].obj
							val statistics = jsonItem["statistics"].obj

							val channelResponse = HttpRequest.get("https://www.googleapis.com/youtube/v3/channels?part=snippet&id=${snippet.get("channelId").asString}&fields=items%2Fsnippet%2Fthumbnails&key=${loritta.config.youtube.apiKey}").body()
							val channelJson = parser.parse(channelResponse).obj

							val viewCount = statistics["viewCount"].string
							val likeCount = statistics["likeCount"].nullString ?: "???"
							val dislikeCount = statistics["dislikeCount"].nullString ?: "???"
							val commentCount = if (statistics.has("commentCount")) {
								statistics["commentCount"].string
							} else {
								"Comentários desativados"
							}

							val thumbnail = snippet["thumbnails"]["high"]["url"].string
							val channelIcon = channelJson["items"][0]["snippet"]["thumbnails"]["high"]["url"].string

							val embed = EmbedBuilder()
							embed.setTitle("<:youtube:314349922885566475> ${Parser.unescapeEntities(item.snippet.title, false)}", "https://youtu.be/${item.id["videoId"].string}")
							embed.setDescription(item.snippet.description)
							embed.addField("⛓ Link", "https://youtu.be/${item.id["videoId"].string}", true)

							embed.addField("\uD83D\uDCFA ${context.legacyLocale["MUSICINFO_VIEWS"]}", viewCount, true)
							embed.addField("\uD83D\uDE0D ${context.legacyLocale["MUSICINFO_LIKES"]}", likeCount, true)
							embed.addField("\uD83D\uDE20 ${context.legacyLocale["MUSICINFO_DISLIKES"]}", dislikeCount, true)
							embed.addField("\uD83D\uDCAC ${context.legacyLocale["MUSICINFO_COMMENTS"]}", commentCount, true)
							embed.setThumbnail(thumbnail)
							embed.setAuthor(item.snippet.channelTitle, "https://youtube.com/channel/${item.snippet.channelId}", channelIcon)

							embed.setColor(Color(217, 66, 52))

							// Criar novo embed!
							mensagem.editMessage(embed.build()).queue()

							context.metadata.put("currentItem", item)
						} else {
							val channelResponse = HttpRequest.get("https://www.googleapis.com/youtube/v3/channels?part=snippet,contentDetails,statistics&id=${item.id["channelId"].string}&key=${loritta.config.youtube.apiKey}").body()

							val channelJson = jsonParser.parse(channelResponse).obj

							val embed = EmbedBuilder()

							val entry = channelJson["items"][0]

							val title = entry["snippet"]["title"].string
							val description = entry["snippet"]["description"].string
							val channelIcon = entry["snippet"]["thumbnails"]["high"]["url"].string
							val viewCount = entry["statistics"]["viewCount"].string
							val subscriberCount = entry["statistics"]["subscriberCount"].string
							val videoCount = entry["statistics"]["videoCount"].string
							val uploadsPlaylistId = if (entry["contentDetails"]["relatedPlaylists"].obj.has("uploads")) {
								entry["contentDetails"]["relatedPlaylists"]["uploads"].string
							} else {
								null
							}
							val likesPlaylistId = if (entry["contentDetails"]["relatedPlaylists"].obj.has("likes")) {
								entry["contentDetails"]["relatedPlaylists"]["likes"].string
							} else {
								null
							}

							var lastUploadedVideoName: String? = null
							var lastUploadedVideoId: String? = null

							var lastLikedVideoName: String? = null
							var lastLikedVideoId: String? = null

							if (uploadsPlaylistId != null) {
								val uploadsPlaylistResponse = HttpRequest.get("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&playlistId=$uploadsPlaylistId&maxResults=1&key=${loritta.config.youtube.apiKey}").body()
								val uploadsPlaylist = jsonParser.parse(uploadsPlaylistResponse).obj

								try {
									lastUploadedVideoName = uploadsPlaylist["items"][0]["snippet"]["title"].string
									lastUploadedVideoId = uploadsPlaylist["items"][0]["snippet"]["resourceId"]["videoId"].string
								} catch (e: Exception) {}
							}

							if (likesPlaylistId != null) {
								val likesPlaylistResponse = HttpRequest.get("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&playlistId=$likesPlaylistId&maxResults=1&key=${loritta.config.youtube.apiKey}").body()
								val likesPlaylist = jsonParser.parse(likesPlaylistResponse).obj

								try {
									lastLikedVideoName = likesPlaylist["items"][0]["snippet"]["title"].string
									lastLikedVideoId = likesPlaylist["items"][0]["snippet"]["resourceId"]["videoId"].string
								} catch (e: Exception) {}
							}

							embed.setTitle(title, "https://youtube.com/channel/${item.snippet.channelId}")
							embed.setThumbnail(channelIcon)
							embed.setDescription(description)
							embed.setColor(Color(217, 66, 52))
							embed.addField("\uD83D\uDCFA ${context.legacyLocale["MUSICINFO_VIEWS"]}", viewCount, true)
							embed.addField("\uD83D\uDC3E ${context.legacyLocale["YOUTUBE_Subscribers"]}", subscriberCount, true)
							embed.addField("\uD83C\uDFA5 ${context.legacyLocale["YOUTUBE_Videos"]}", videoCount, true)

							if (lastUploadedVideoName != null)
								embed.addField("\uD83D\uDCE5 ${context.legacyLocale["YOUTUBE_LastUploadedVideo"]}", "[$lastUploadedVideoName](https://youtu.be/$lastUploadedVideoId)", true)
							if (lastLikedVideoName != null)
								embed.addField("<:starstruck:540988091117076481> ${context.legacyLocale["YOUTUBE_LastLikedVideo"]}", "[$lastLikedVideoName](https://youtu.be/$lastLikedVideoId)", true)

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