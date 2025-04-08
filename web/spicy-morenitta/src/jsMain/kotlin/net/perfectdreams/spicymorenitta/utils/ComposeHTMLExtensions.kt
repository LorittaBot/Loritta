package net.perfectdreams.spicymorenitta.utils

import org.jetbrains.compose.web.attributes.AttrsScope
import web.html.HTMLElement

/**
 * Sets a plain CSS style string to an element
 *
 * @param style the style
 */
fun AttrsScope<HTMLElement>.plainStyle(style: String) {
    attr("style", style)
}