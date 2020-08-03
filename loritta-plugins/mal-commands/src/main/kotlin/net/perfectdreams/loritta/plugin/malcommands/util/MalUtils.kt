package net.perfectdreams.loritta.plugin.malcommands.util

import com.mrpowergamerbr.loritta.utils.encodeToUrl
import mu.KotlinLogging
import net.perfectdreams.loritta.plugin.malcommands.commands.models.AnimeInfo
import net.perfectdreams.loritta.plugin.malcommands.commands.models.AnimeStatus
import net.perfectdreams.loritta.plugin.malcommands.commands.models.AnimeType
import net.perfectdreams.loritta.plugin.malcommands.commands.models.MalAnime
import net.perfectdreams.loritta.plugin.malcommands.exceptions.MalException
import net.perfectdreams.loritta.plugin.malcommands.exceptions.MalSearchException
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
        logger.debug { "Made request to $req" }

        // Cover all the client and server errors
        if (response.statusCode() in 400..599)
            return null

        return response.parse()
    }

    private fun queryAnime(q: String): String? {
        val document = requestDom("anime.php?q=${q.encodeToUrl()}")
        try {
            // The first article would be the anime queries
            // For now, we only need the first anime found from queries
            val animeArticle = document!!.select("table[cellpadding=\"0\"][cellspacing=\"0\"] > tbody > tr > td > a[href][class*=\"hoverinfo_trigger\"]").first()
            // Now we just need to get the "a" element and then, get the "href" attribute
            if (animeArticle != null) logger.debug { "Got the element \"a\"!" }
            val result = animeArticle!!.attributes()["href"]

            return if (MalConstants.MAL_ANIMEURL_REGEX.matches(result)) {
                result
            } else {
                null
            }
        } catch (e: Exception) {
            // Exceptions are cool for knowing what happened
            throw MalSearchException(cause = e)
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
                    image = document.selectFirst("img[itemprop=\"image\"][alt=\"${animeInfo.name}\"]")
                            .attr("data-src"),
                    score = document.selectFirst(".score-label").text(),
                    synopsis = document.selectFirst("span[itemprop=\"description\"]").text(),
                    rank = document.selectFirst("span.ranked").text()
                            .split(' ').drop(1).first(),
                    popularity = u.getContentBySpan("Popularity:")!!

            )
        } catch(e: Exception) {
            throw MalException("Failed at parsing/scrapping anime page", e)
        }
    }

    fun parseAnimeByQuery(q: String): MalAnime? {
        return parseAnime(this.queryAnime(q)!!)
    }
}
