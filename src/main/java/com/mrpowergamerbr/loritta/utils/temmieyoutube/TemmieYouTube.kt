package com.mrpowergamerbr.loritta.utils.temmieyoutube

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.Loritta
import org.apache.commons.lang3.StringUtils
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

class TemmieYouTube(private val apiKey: String) {
	/**
	 * Pesquisa algo no YouTube
	 *
	 * @return O resultado da pesquisa
	 */
	fun searchOnYouTube(searchQuery: String): SearchResponse {
		val params = HashMap<String, Any>()
		params.put("part", "snippet")
		params.put("q", searchQuery)
		params.put("key", apiKey)
		val req = HttpRequest.get("https://www.googleapis.com/youtube/v3/search?" + buildQuery(params))

		val body = req.body()

		return Loritta.gson.fromJson(body, SearchResponse::class.java)
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
		val pageInfo: String,
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