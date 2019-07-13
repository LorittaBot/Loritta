package net.perfectdreams.spicymorenitta.utils

import org.w3c.dom.*
import org.w3c.dom.events.Event

fun <T> ParentNode.select(query: String): T {
    return this.querySelector(query) as T
}

fun <T> ParentNode.selectAll(query: String): List<T> {
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