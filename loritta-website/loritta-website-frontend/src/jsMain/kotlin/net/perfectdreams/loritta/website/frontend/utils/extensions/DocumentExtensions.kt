package net.perfectdreams.loritta.website.frontend.utils.extensions

import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.ParentNode
import org.w3c.dom.asList
import org.w3c.dom.events.Event

inline fun <T> ParentNode.select(query: String): T {
    return this.querySelector(query) as T
}

inline fun <T> ParentNode.selectAll(query: String): List<T> {
    return this.querySelectorAll(query).asList() as List<T>
}

fun Element.onClick(callback: (Event) -> (Unit)) {
    this.addEventListener("click", callback)
}

fun Element.offset(): Offset {
    val rect = this.getBoundingClientRect()
    val scrollLeft = window.pageXOffset
    val scrollTop = window.pageYOffset
    return Offset(
        rect.top + scrollTop,
        rect.left + scrollLeft
    )
}

data class Offset(
    val top: Double,
    val left: Double
)