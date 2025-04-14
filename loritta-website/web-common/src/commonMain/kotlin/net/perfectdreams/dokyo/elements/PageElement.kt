package net.perfectdreams.dokyo.elements

import kotlinx.html.HTMLTag

class PageElement(val id: String) {
    fun apply(div: HTMLTag) {
        div.attributes["id"] = id
    }
}