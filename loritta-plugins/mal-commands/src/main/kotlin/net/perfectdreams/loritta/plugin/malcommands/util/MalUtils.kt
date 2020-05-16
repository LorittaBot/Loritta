package net.perfectdreams.loritta.plugin.malcommands.util

import com.mrpowergamerbr.loritta.utils.encodeToUrl
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object MalUtils {
    private const val MAL_URL = "https://myanimelist.net/"

    private fun requestDom(endpoint: String): Document? {
        val response = Jsoup.connect("${MAL_URL}${endpoint.replace(MAL_URL, "")}")
                .ignoreHttpErrors(true)
                .execute()

        if (response.statusCode() == 404)
            return null

        return response.parse()
    }
    fun queryAnime(q: String): String? {
        val document = requestDom("search/all?q=${q.encodeToUrl()}")
        return try {
            // o primeiro article seria as queries de anime
            val animeArticle = document!!.selectFirst("article")
            // só precisamos pegar o primeiro anime que vier xisde
            val firstList = animeArticle.getElementsByClass("list").firstOrNull()
            val infoList = firstList!!.getElementsByClass("information")!!.firstOrNull()
            // Só nos resta pegar o elemento "a" que está com o atributo que nós leva à página do anime
            infoList!!.select("a").attr("href")
        } catch(e: Exception) {
            null
        }
    }
    fun parseAnime(url: String): MalAnime? {
        val document = requestDom(url)
        val u = MalScrappingUtils
        return try {
            val anime = MalAnime(
                    info = AnimeInfo(
                            name = document!!.select("span").attr("itemprop"),
                            type = when (u.getContentBySpan(document, "Type:")) {
                                "TV" -> AnimeType.TV
                                "ONA" -> AnimeType.ONA
                                "Movie" -> AnimeType.MOVIE
                                "OVA" -> AnimeType.OVA
                                "Special" -> AnimeType.SPECIAL
                                else -> AnimeType.UNKNOWN
                            },
                            episodes = u.getContentBySpan(document, "Episodes:")?.toInt(),

                    ),
                    score = document.getElementsByClass("score-label").text()
            )
            TODO("fazer todo o paranue")
        } catch (e: Exception) {
            null
        }
    }
}