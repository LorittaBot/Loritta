package net.perfectdreams.loritta.website.backend.utils.extras.dynamic

import kotlinx.html.DIV

/**
 * Used to generate dynamically extras entries, useful for pages that are easier to be done by generating
 * the content in content instead of markdown
 */
abstract class DynamicExtras(
    val path: String,
    val title: String,
    val authors: List<String>
) {
    abstract fun generateContent(tag: DIV)
}