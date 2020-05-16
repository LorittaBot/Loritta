package net.perfectdreams.loritta.plugin.malcommands.util

import org.jsoup.nodes.Document

object MalScrappingUtils {
    // gambiarra that might work?
    fun getContentBySpan(document: Document, q: String): String? {
        return document.select("span:contains(${q})")
                .parents()
                .first()
                .text()
                .trim()
                .split(':')
                .toTypedArray()[1]
    }
}