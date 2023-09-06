package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

import org.w3c.dom.parsing.DOMParser

/**
 * Strips HTML from the [input]
 */
// https://stackoverflow.com/questions/822452/strip-html-tags-from-text-using-plain-javascript
fun stripHTML(input: String): String {
    val doc = DOMParser().parseFromString(input, "text/html");
    return doc.body?.textContent ?: ""
}