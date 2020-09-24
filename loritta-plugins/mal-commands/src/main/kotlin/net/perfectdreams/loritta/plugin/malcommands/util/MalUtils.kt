package net.perfectdreams.loritta.plugin.malcommands.util

import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.utils.encodeToUrl
import com.mrpowergamerbr.loritta.utils.loritta
import io.ktor.client.request.*
import mu.KotlinLogging
import net.perfectdreams.loritta.plugin.malcommands.commands.models.AnimeInfo
import net.perfectdreams.loritta.plugin.malcommands.commands.models.AnimeStatus
import net.perfectdreams.loritta.plugin.malcommands.commands.models.AnimeType
import net.perfectdreams.loritta.plugin.malcommands.commands.models.MalAnime
import net.perfectdreams.loritta.plugin.malcommands.exceptions.MalException
import net.perfectdreams.loritta.plugin.malcommands.util.MalConstants.MAL_URL
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import com.github.salomonbrys.kotson.*

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

    suspend fun queryAnime(query: String): List<String> {
        val response = loritta.http.get<HttpResponseData>(
                "${MAL_URL}search/prefix.json?type=anime&keyword=${query.encodeToUrl()}&v=1"
        )
        val parsed = JsonParser.parseString(response.body as String)

        return parsed["categories"]["items"].array.map { it["url"].string }
    }

    fun parseAnime(url: String): MalAnime? = try {
            val document = requestDom(url)
            val utils = MalScrappingUtils(document)

            val animeInfo = AnimeInfo(
                    name = document!!.getElementsByClass("title-name").text().trim(),
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

            MalAnime(
                    url = url,
                    info = animeInfo,
                    image = document.selectFirst("img[itemprop=\"image\"][alt=\"${animeInfo.name}\"]")
                            .attr("data-src"),
                    score = document.selectFirst(".score-label").text(),
                    synopsis = document.selectFirst("span[itemprop=\"description\"]").text(),
                    rank = document.selectFirst("span.ranked").text()
                            .split(' ').drop(1).first(),
                    popularity = utils.getContentBySpan("Popularity:")!!

            )
        } catch(e: Exception) {
            throw MalException("Failed at parsing/scrapping anime page", e)
        }

}
