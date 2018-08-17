package com.mrpowergamerbr.loritta.utils.misc

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.temmieyoutube.SearchResponse
import com.mrpowergamerbr.loritta.utils.temmieyoutube.YouTubeItem
import kotlin.streams.toList

object YouTubeUtils {
	/**
	 * Procura vídeos no YouTube e retorna o SearchResponse
	 *
	 * @param query  O que você deseja pesquisar no YouTube
	 * *
	 * @return O SearchResponse
	 */
	@JvmStatic
	fun searchOnYouTube(query:String):SearchResponse {
		val response = Loritta.youtube.searchOnYouTube(query)
		return response
	}

	/**
	 * Procura canais, vídeos, livestrams, etc no YouTube e retorna a lista com os resultados
	 *
	 * @param query  O que você deseja pesquisar no YouTube
	 *
	 * @return uma lista com os resultados
	 */
	@JvmStatic
	fun searchAnythingOnYouTube(query:String):List<YouTubeItem> {
		val (_, _, _, _, _, items) = searchOnYouTube(query)
		return items
	}

	/**
	 * Procura vídeos no YouTube e retorna uma lista com os resultados
	 *
	 * @param query  O que você deseja pesquisar no YouTube
	 *
	 * @return uma lista com os resultados
	 */
	@JvmStatic
	fun searchVideosOnYouTube(query: String):List<YouTubeItem> {
		val response = searchAnythingOnYouTube(query)
		return response.stream().filter{ it.id.kind == "youtube#video"}.toList()
	}

	/**
	 * Procura vídeos no YouTube e retorna uma lista com os resultados
	 *
	 * @param query  O que você deseja pesquisar no YouTube
	 *
	 * @return uma lista com os resultados
	 */
	@JvmStatic
	fun searchOnYouTube(query: String, vararg kinds: String): List<YouTubeItem> {
		val response = searchAnythingOnYouTube(query)
		return response.stream().filter{ kinds.contains(it.id.kind)}.toList()
	}
}
