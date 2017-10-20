package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.MiscUtils
import com.mrpowergamerbr.loritta.utils.YouTubeUtils
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.msgFormat
import com.mrpowergamerbr.loritta.utils.temmieyoutube.YouTubeItem
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import java.awt.Color
import java.util.*

class YouTubeCommand : CommandBase() {
	val indexes = listOf("1⃣",
			"2⃣",
			"3⃣",
			"4⃣",
			"5⃣")
	override fun getLabel(): String {
		return "youtube"
	}

	override fun getAliases() : List<String> {
		return listOf("yt")
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale["YOUTUBE_DESCRIPTION"]
	}

	override fun getExample(): List<String> {
		return Arrays.asList("shantae tassel town")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN
	}

	override fun onlyInMusicInstance(): Boolean {
		return true
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			var query = context.args.joinToString(" ");
			val items = YouTubeUtils.searchOnYouTube(query, "youtube#video", "youtube#channel")

			if (items.isNotEmpty()) {
				var format = "";
				var youtubeKey = loritta.youtubeKey
				for (i in 0 until Math.min(5, items.size)) {
					var item = items[i];
					if (item.id.kind == "youtube#video") {
						var response = HttpRequest.get("https://www.googleapis.com/youtube/v3/videos?id=${item.id.videoId}&part=contentDetails&key=${youtubeKey}").body();
						var parser = jsonParser
						var json = parser.parse(response).asJsonObject
						var strDuration = json["items"].array[0]["contentDetails"]["duration"].string
						var duration = java.time.Duration.parse(strDuration)
						var inSeconds = duration.get(java.time.temporal.ChronoUnit.SECONDS); // Nós não podemos pegar o tempo diretamente porque é "unsupported"
						var final = String.format("%02d:%02d", ((inSeconds / 60) % 60), (inSeconds % 60));
						format += "${indexes[i]}\uD83C\uDFA5 `[${final}]` **[${item.snippet.title}](https://youtu.be/${item.id.videoId})**\n";
					} else {
						format += "${indexes[i]}\uD83D\uDCFA **[${item.snippet.title}](https://youtu.be/${item.id.videoId})**\n";
					}
					context.metadata.put(i.toString(), item);
				}
				var embed = EmbedBuilder();
				embed.setColor(Color(217, 66, 52));
				embed.setDescription(format);
				embed.setTitle("<:youtube:314349922885566475> ${context.locale.YOUTUBE_RESULTS_FOR.msgFormat(query)}");
				var mensagem = context.sendMessage(context.getAsMention(true), embed.build());
				// Adicionar os reactions
				for (i in 0 until Math.min(5, items.size)) {
					mensagem.addReaction(indexes[i]).complete();
				}
				return;
			} else {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale.YOUTUBE_COULDNT_FIND.msgFormat(query))
			}
		} else {
			context.explain()
		}
	}

	override fun onCommandReactionFeedback(context: CommandContext, e: GenericMessageReactionEvent, msg: Message) {
		if (e.user == context.userHandle) { // Somente quem executou o comando pode interagir!
			if (context.metadata.contains("currentItem")) {
				var item = context.metadata["currentItem"] as YouTubeItem;
				if (e.reactionEmote.name == "▶") {
					if (loritta.checkAndLoad(context, "https://youtu.be/${item.id.videoId}")) {
						context.metadata.remove("currentItem")
					}
				}

				if (!context.metadata.containsKey("downloading") && e.reactionEmote.name == "\uD83D\uDCE5") {
					context.metadata["downloading"] = true
					MiscUtils.sendYouTubeVideoMp3(context, "https://youtu.be/${item.id.videoId}")
				}
				return
			}

			var item: YouTubeItem;
			if (e.reactionEmote.name == "1⃣") {
				item = context.metadata["0"] as YouTubeItem;
			} else if (e.reactionEmote.name == "2⃣") {
				item = context.metadata["1"] as YouTubeItem;
			} else if (e.reactionEmote.name == "3⃣") {
				item = context.metadata["2"] as YouTubeItem;
			} else if (e.reactionEmote.name == "4⃣") {
				item = context.metadata["3"] as YouTubeItem;
			} else {
				item = context.metadata["4"] as YouTubeItem;
			}

			// Remover todos os reactions
			msg.clearReactions().complete();

			if (item.id.kind == "youtube#video") { // Se é um vídeo...
				val response = HttpRequest.get("https://www.googleapis.com/youtube/v3/videos?id=${item.id.videoId}&part=snippet,statistics&key=${loritta.youtubeKey}").body();
				val parser = JsonParser();
				val json = parser.parse(response).asJsonObject;
				val jsonItem = json["items"][0]
				val snippet = jsonItem["snippet"].obj
				val statistics = jsonItem["statistics"].obj

				var channelResponse = HttpRequest.get("https://www.googleapis.com/youtube/v3/channels?part=snippet&id=${snippet.get("channelId").asString}&fields=items%2Fsnippet%2Fthumbnails&key=${loritta.youtubeKey}").body();
				var channelJson = parser.parse(channelResponse).obj;

				val viewCount = statistics["viewCount"].string
				val likeCount = statistics["likeCount"].string
				val dislikeCount = statistics["dislikeCount"].string
				val commentCount = if (statistics.has("commentCount")) {
					statistics["commentCount"].string
				} else {
					"Comentários desativados"
				}

				val thumbnail = snippet["thumbnails"]["high"]["url"].string
				val channelIcon = channelJson["items"][0]["snippet"]["thumbnails"]["high"]["url"].string

				var embed = EmbedBuilder()
				embed.setTitle("<:youtube:314349922885566475> ${item.snippet.title}", "https://youtu.be/${item.id.videoId}")
				embed.setDescription(item.snippet.description)
				embed.addField("⛓ Link", "https://youtu.be/${item.id.videoId}", true)

				embed.addField("\uD83D\uDCFA ${context.locale["MUSICINFO_VIEWS"]}", viewCount, true)
				embed.addField("\uD83D\uDE0D ${context.locale["MUSICINFO_LIKES"]}", likeCount, true)
				embed.addField("\uD83D\uDE20 ${context.locale["MUSICINFO_DISLIKES"]}", dislikeCount, true)
				embed.addField("\uD83D\uDCAC ${context.locale["MUSICINFO_COMMENTS"]}", commentCount, true)
				embed.setThumbnail(thumbnail)
				embed.setAuthor("${item.snippet.channelTitle}", "https://youtube.com/channel/${item.snippet.channelId}", channelIcon)

				embed.setColor(Color(217, 66, 52));

				// Criar novo embed!
				msg.editMessage(embed.build()).complete();

				context.metadata.put("currentItem", item);

				if (context.config.musicConfig.isEnabled) {
					// Se o sistema de músicas está ativado...
					msg.addReaction("▶").complete(); // Vamos colocar um ícone para tocar!
				}

				msg.addReaction("\uD83D\uDCE5").complete(); // Adicionar ícone para baixar a música em MP3
			} else {
				var channelResponse = HttpRequest.get("https://www.googleapis.com/youtube/v3/channels?part=snippet,contentDetails,statistics&id=${item.snippet.channelId}&key=${loritta.youtubeKey}").body()
				var channelJson = jsonParser.parse(channelResponse).obj

				var embed = EmbedBuilder()

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
					var uploadsPlaylistResponse = HttpRequest.get("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&playlistId=$uploadsPlaylistId&maxResults=1&key=${loritta.youtubeKey}").body()
					var uploadsPlaylist = jsonParser.parse(uploadsPlaylistResponse).obj

					lastUploadedVideoName = uploadsPlaylist["items"][0]["snippet"]["title"].string
					lastUploadedVideoId = uploadsPlaylist["items"][0]["snippet"]["resourceId"]["videoId"].string
				}

				if (likesPlaylistId != null) {
					var likesPlaylistResponse = HttpRequest.get("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&playlistId=$likesPlaylistId&maxResults=1&key=${loritta.youtubeKey}").body()
					var likesPlaylist = jsonParser.parse(likesPlaylistResponse).obj

					lastLikedVideoName = likesPlaylist["items"][0]["snippet"]["title"].string
					lastLikedVideoId = likesPlaylist["items"][0]["snippet"]["resourceId"]["videoId"].string
				}

				embed.setTitle(title, "https://youtube.com/channel/${item.snippet.channelId}")
				embed.setThumbnail(channelIcon)
				embed.setDescription(description)
				embed.setColor(Color(217, 66, 52))
				embed.addField("\uD83D\uDCFA ${context.locale["MUSICINFO_VIEWS"]}", viewCount, true)
				embed.addField("\uD83D\uDC3E ${context.locale["YOUTUBE_Subscribers"]}", subscriberCount, true)
				embed.addField("\uD83C\uDFA5 ${context.locale["YOUTUBE_Videos"]}", videoCount, true)

				if (lastUploadedVideoName != null)
					embed.addField("\uD83D\uDCE5 ${context.locale["YOUTUBE_LastUploadedVideo"]}", "[$lastUploadedVideoName](https://youtu.be/$lastUploadedVideoId)", true)
				if (lastLikedVideoName != null)
					embed.addField("<:twitt_starstruck:352216844603752450> ${context.locale["YOUTUBE_LastLikedVideo"]}", "[$lastLikedVideoName](https://youtu.be/$lastLikedVideoId)", true)

				embed.addField("⛓ Link", "https://youtube.com/channel/${item.snippet.channelId}", true)
				// Criar novo embed!
				msg.editMessage(embed.build()).complete()
			}
		}
	}
}