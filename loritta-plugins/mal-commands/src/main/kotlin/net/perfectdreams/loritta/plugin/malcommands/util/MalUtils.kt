package net.perfectdreams.loritta.plugin.malcommands.util

import com.mrpowergamerbr.loritta.utils.encodeToUrl
import mu.KotlinLogging
import net.perfectdreams.loritta.plugin.malcommands.util.MalConstants.MAL_URL
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object MalUtils {

    private val logger = KotlinLogging.logger { }

    private fun requestDom(endpoint: String): Document? {
        val req = "${MAL_URL}${endpoint.replace(MAL_URL, "")}"
        val response = Jsoup.connect(req)
                .ignoreHttpErrors(true)
                .execute()
        logger.debug { "Made request to ${req}" }

        if (response.statusCode() == 404)
            return null

        return response.parse()
    }

    private fun queryAnime(q: String): String? {
        val document = requestDom("search/all?q=${q.encodeToUrl()}")
        return try {
            // The first article found would be animes page
            // For now, we only need the first anime found from queries
            val animeArticle = document!!.selectFirst("article > .list > .information > a")
            // Só nos resta pegar o elemento "a" que está com o atributo que nós leva à página do anime
            if (animeArticle != null)
                logger.debug { "Got the element \"a\"!" }
            animeArticle!!.attr("href")
        } catch (e: Exception) {
            null
        }
    }

    fun parseAnime(url: String): MalAnime? {
        val document = requestDom(url)
        val u = MalScrappingUtils(document)
        return try {
            // Workarounds:
            // This is needed for some animes with english titles
            document!!.selectFirst("span[itemprop=\"name\"] span")?.remove()
            val animeInfo = AnimeInfo(
                    name = document.selectFirst("span[itemprop=\"name\"]").text().trim(),
                    type = when (u.getContentBySpan("Type:")) {
                        "TV" -> AnimeType.TV
                        "ONA" -> AnimeType.ONA
                        "Movie" -> AnimeType.MOVIE
                        "OVA" -> AnimeType.OVA
                        "Special" -> AnimeType.SPECIAL
                        else -> AnimeType.UNKNOWN
                    },
                    status = when (u.getContentBySpan("Status:")) {
                        "Finished Airing" -> AnimeStatus.FINISHED_AIRING
                        "Currently Airing" -> AnimeStatus.CURRENTLY_AIRING
                        "Not yet aired" -> AnimeStatus.NOT_YET_AIRED
                        else -> AnimeStatus.UNKNOWN
                    },
                    aired = u.getContentBySpan("Aired:"),
                    episodes = u.getContentBySpan("Episodes:")?.toInt(),
                    source = u.getContentBySpan("Source:"),
                    genres = document.select("span[itemprop=\"genre\"]").map { it.text() }
            )

            MalAnime(
                    url = url,
                    info = animeInfo,
                    image = document.selectFirst("img[itemprop=\"image\"][alt=\"${animeInfo.name}\"]").attr("data-src"),
                    score = document.selectFirst(".score-label").text(),
                    synopsis = document.selectFirst("span[itemprop=\"description\"]").text(),
                    rank = document.selectFirst("span.ranked").text()
                            .split(' ').drop(1).first(),
                    popularity = u.getContentBySpan("Popularity:")

            )
        } catch (e: Exception) {
            logger.debug { e }
            null
        }
    }

    fun parseAnimeByQuery(q: String): MalAnime? {
        return this.parseAnime(this.queryAnime(q)!!)
    }
}
