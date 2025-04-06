package net.perfectdreams.spicymorenitta.utils

import js.array.asList
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.*
import org.w3c.dom.events.Event

inline fun <T> ParentNode.select(query: String): T {
    return this.querySelector(query) as T
}

inline fun <T> ParentNode.selectAll(query: String): List<T> {
    return this.querySelectorAll(query).asList() as List<T>
}

inline fun <T> web.dom.ParentNode.querySelector(query: String): T {
    return this.querySelector(query) as T
}

inline fun <T> web.dom.ParentNode.querySelectorAll(query: String): List<T> {
    return this.querySelectorAll(query).asList() as List<T>
}

// https://stackoverflow.com/a/59220393/7271796
fun Document.onDOMContentLoaded(callback: () -> (Unit)) {
    println("Current document readyState is ${document.readyState}")
    if (document.readyState == DocumentReadyState.INTERACTIVE || document.readyState == DocumentReadyState.COMPLETE) {
        // already fired, so run logic right away
        callback.invoke()
    } else {
        // not fired yet, so let's listen for the event
        this.addEventListener("DOMContentLoaded", { callback.invoke() }, false)
    }
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

val visibleModal: Element
    get() = page.getElementByClass("tingle-modal--visible")