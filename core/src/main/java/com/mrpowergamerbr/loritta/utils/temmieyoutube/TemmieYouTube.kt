package com.mrpowergamerbr.loritta.utils.temmieyoutube

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.obj
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.MiscUtils
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import org.apache.commons.lang3.StringUtils
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

class TemmieYouTube {
	/**
	 * Pesquisa algo no YouTube
	 *
	 * @return O resultado da pesquisa
	 */
	fun searchOnYouTube(searchQuery: String): SearchResponse {
		val key = loritta.youtubeKey
		val params = HashMap<String, Any>()
		params.put("part", "snippet")
		params.put("q", searchQuery)
		params.put("key", key)
		val req = HttpRequest.get("https://www.googleapis.com/youtube/v3/search?" + buildQuery(params))

		val body = req.body()

		var searchJson = jsonParser.parse(body).obj
		val responseError = MiscUtils.getResponseError(searchJson)
		val error = responseError == "dailyLimitExceeded" || responseError == "quotaExceeded"

		if (error) {
			loritta.youtubeKeys.remove(key)
			if (loritta.youtubeKeys.isNotEmpty()) {
				return searchOnYouTube(searchQuery)
			} else {
				return null!!
			}
		}

		return Loritta.GSON.fromJson(body, SearchResponse::class.java)
	}

	private fun buildQuery(params: Map<String, Any>): String {
		val query = arrayOfNulls<String>(params.size)
		var index = 0
		for (key in params.keys) {
			var `val` = (if (params[key] != null) params[key] else "").toString()
			try {
				`val` = URLEncoder.encode(`val`, "UTF-8")
			} catch (e: UnsupportedEncodingException) {
			}

			query[index++] = key + "=" + `val`
		}

		return StringUtils.join(query, "&")
	}
}

data class SearchResponse(
		val kind: String,
		val etag: String,
		val nextPageToken: String,
		val regionCode: String,
		val pageInfo: PageInfo,
		val items: List<YouTubeItem>)

data class YouTubeItem(
		val kind: String,
		val etag: String,
		val id: YouTubeId,
		val snippet: Snippet)

data class Thumbnails(
		val default: ThumbnailInfo,
		val medium: ThumbnailInfo,
		val high: ThumbnailInfo)

data class Snippet(
		val publishedAt: String,
		val channelId: String,
		val title: String,
		val description: String,
		val thumbnails: Thumbnails,
		val channelTitle: String,
		val liveBroadcastContent: String)

data class PageInfo(
		val totalResults: Int,
		val resultsPerPage: Int)

data class ThumbnailInfo(
		val url: String,
		val width: Int,
		val height: Int)

data class YouTubeId(
		val kind: String,
		val videoId: String)