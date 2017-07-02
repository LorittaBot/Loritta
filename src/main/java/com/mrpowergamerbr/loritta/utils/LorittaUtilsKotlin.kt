package com.mrpowergamerbr.loritta.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.music.AudioTrackWrapper
import java.awt.Image
import java.awt.image.BufferedImage
import java.text.DateFormatSymbols
import java.time.OffsetDateTime

inline fun OffsetDateTime.humanize(): String {
	val months = DateFormatSymbols().getMonths();
	return "${this.dayOfMonth} de ${months[this.month.value - 1]}, ${this.year} às ${this.hour.toString().padStart(2, '0')}:${this.minute.toString().padStart(2, '0')}";
}

inline fun Image.toBufferedImage() : BufferedImage {
	return ImageUtils.toBufferedImage(this)
}

inline fun BufferedImage.makeRoundedCorners(cornerRadius: Int) : BufferedImage {
	return ImageUtils.makeRoundedCorner(this, cornerRadius);
}

object LorittaUtilsKotlin {
	@JvmStatic
	fun fillTrackMetadata(track: AudioTrackWrapper) {
		if (track.track.sourceManager.sourceName == "youtube") { // Se é do YouTube, então vamos preencher com algumas informações "legais"
			val playingTrack = track.track;
			val videoId = playingTrack.info.uri.substring(playingTrack.info.uri.length - 11..playingTrack.info.uri.length - 1)
			val response = HttpRequest.get("https://www.googleapis.com/youtube/v3/videos?id=${videoId}&part=snippet,statistics&key=${Loritta.config.youtubeKey}").body();
			val parser = JsonParser();
			val json = parser.parse(response).asJsonObject;
			val item = json.get("items").asJsonArray.get(0)
			val snippet = item.get("snippet").asJsonObject
			val statistics = item.get("statistics").asJsonObject

			var channelResponse = HttpRequest.get("https://www.googleapis.com/youtube/v3/channels?part=snippet&id=${snippet.get("channelId").asString}&fields=items%2Fsnippet%2Fthumbnails&key=${Loritta.config.youtubeKey}").body();
			var channelJson = parser.parse(channelResponse).asJsonObject;

			track.metadata.put("viewCount", statistics.get("viewCount").asString)
			track.metadata.put("likeCount", statistics.get("likeCount").asString)
			track.metadata.put("dislikeCount", statistics.get("dislikeCount").asString)
			track.metadata.put("commentCount", statistics.get("commentCount").asString)
			track.metadata.put("thumbnail", snippet.get("thumbnails").asJsonObject.get("high").asJsonObject.get("url").asString)
			track.metadata.put("channelIcon", channelJson.get("items").asJsonArray[0].asJsonObject.get("snippet").asJsonObject.get("thumbnails").asJsonObject.get("high").asJsonObject.get("url").asString)
		}
	}
}