package net.perfectdreams.loritta.plugin.malcommands.util

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.utils.encodeToUrl
import mu.KotlinLogging
import net.perfectdreams.loritta.plugin.malcommands.models.AnimeInfo
import net.perfectdreams.loritta.plugin.malcommands.models.AnimeStatus
import net.perfectdreams.loritta.plugin.malcommands.models.AnimeType
import net.perfectdreams.loritta.plugin.malcommands.models.MalAnime
import net.perfectdreams.loritta.plugin.malcommands.util.MalConstants.MAL_URL
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object MalUtils {
    private val logger = KotlinLogging.logger { }

    private fun requestDom(endpoint: String): Document? {
        val response = Jsoup.connect(endpoint)
                .ignoreHttpErrors(true)
                .execute()

        logger.debug { "Made request to $endpoint" }

        // Cover all the client and server errors
        if (response.statusCode() in 400..599) return null

        return response.parse()
    }

    fun queryAnime(query: String): List<String?> {
        val response = HttpRequest.get(
                "${MAL_URL}search/prefix.json?type=anime&keyword=${query.encodeToUrl()}&v=1"
        ).body()
        val parsed = JsonParser.parseString(response)

        return parsed.obj["categories"].array[0].obj["items"].array.map { it.obj["url"].string }
    }

    fun parseAnime(url: String): MalAnime? {
        val document = requestDom(url)
        val utils = MalScrappingUtils(document)

        val animeInfo = AnimeInfo(
                name = document!!.selectFirst(".title-name").text().trim(),
                type = when (utils.getContentBySpan("Type:")) {
                    "TV" -> AnimeType.TV
                    "ONA" -> AnimeType.ONA
                    "Movie" -> AnimeType.MOVIE
                    "OVA" -> AnimeType.OVA
                    "Special" -> AnimeType.SPECIAL
                    else -> AnimeType.UNKNOWN
                },
                status = when (utils.getContentBySpan("Status:")) {
                    "Finished Airing" -> AnimeStatus.FINISHED_AIRING
                    "Currently Airing" -> AnimeStatus.CURRENTLY_AIRING
                    "Not yet aired" -> AnimeStatus.NOT_YET_AIRED
                    else -> AnimeStatus.UNKNOWN
                },
                aired = utils.getContentBySpan("Aired:"),
                episodes = utils.getContentBySpan("Episodes:")?.toInt(),
                source = utils.getContentBySpan("Source:"),
                genres = document.select("span[itemprop=\"genre\"]").map { it.text() }
        )

        return MalAnime(
                url = url,
                info = animeInfo,
                image = document.selectFirst("img[itemprop=\"image\"][alt=\"${animeInfo.name}\"]")
                        .attr("data-src"),
                score = document.selectFirst(".score-label").text(),
                synopsis = document.selectFirst("p[itemprop=\"description\"]").text(),
                rank = document.selectFirst("span.ranked").text()
                        .split(' ').drop(1).first(),
                popularity = utils.getContentBySpan("Popularity:")!!
        )
    }

}
