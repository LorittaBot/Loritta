package net.perfectdreams.loritta.website.frontend.utils.extensions

import js.array.asList
import web.dom.Element
import web.dom.ParentNode
import web.events.addEventListener
import web.uievents.MouseEvent
import web.window.window

fun <T> ParentNode.select(query: String): T {
    return this.querySelector(query) as T
}

fun <T> ParentNode.selectAll(query: String): List<T> {
    return this.querySelectorAll(query).asList() as List<T>
}

fun Element.onClick(callback: (MouseEvent) -> (Unit)) {
    this.addEventListener(MouseEvent.CLICK, callback)
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