package net.perfectdreams.loritta.embededitor

import io.ktor.util.InternalAPI
import org.w3c.dom.*
import org.w3c.dom.events.Event
import kotlin.browser.document
import kotlin.browser.window

fun main() {
    document.onDOMReady {
        val embedEditor = EmbedEditor()
        embedEditor.start()
    }
}

inline fun <T> ParentNode.select(query: String): T {
    return this.querySelector(query) as T
}

inline fun <T> ParentNode.selectAll(query: String): List<T> {
    return this.querySelectorAll(query).asList() as List<T>
}

fun Document.onDOMReady(callback: (Event) -> (Unit)) {
    this.addEventListener("DOMContentLoaded", callback, false)
}

fun Element.onClick(callback: (Event) -> (Unit)) {
    this.addEventListener("click", callback)
}

fun Window.onScroll(callback: (Event) -> (Unit)) {
    this.addEventListener("scroll", callback)
}

fun Element.onScroll(callback: (Event) -> (Unit)) {
    this.addEventListener("scroll", callback)
}

fun Element.onMouseOver(callback: (Event) -> (Unit)) {
    this.addEventListener("mouseover", callback)
}

fun Element.onMouseOut(callback: (Event) -> (Unit)) {
    this.addEventListener("mouseout", callback)
}

fun Element.onMouseEnter(callback: (Event) -> (Unit)) {
    this.addEventListener("mouseenter", callback)
}

fun Element.onMouseLeave(callback: (Event) -> (Unit)) {
    this.addEventListener("mouseleave", callback)
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

fun Element.width() = this.getBoundingClientRect().width

data class Offset(
        val top: Double,
        val left: Double
)