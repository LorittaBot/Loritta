package net.perfectdreams.loritta.website.views

import kotlinx.html.div
import kotlinx.html.dom.create
import org.w3c.dom.Document
import org.w3c.dom.Element

class FanArtsView(document: Document) : HasNavbarView(document) {
    override fun getTitle(): String {
        return "Fan Arts!"
    }

    override fun generateContent(): Element {
        return document.create.div {}
    }
}