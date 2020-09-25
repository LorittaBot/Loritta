package net.perfectdreams.loritta.plugin.malcommands.util

import mu.KotlinLogging
import org.jsoup.nodes.Document
class MalScrappingUtils(private val document: Document?) {
    private val logger = KotlinLogging.logger { }

    // https://www.urbandictionary.com/define.php?term=Gambiarra
    fun getContentBySpan(q: String): String? {
        val content = document!!.selectFirst("span:contains(${q})")
                .parents()
                .first()
                .text()
                .trim()
                .replace(q, "")
                .trim()
        logger.debug { content }
        return if (content != "Unknown") {
            content
        } else {
            null
        }
    }
}