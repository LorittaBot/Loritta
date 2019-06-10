package net.perfectdreams.loritta.website.views

import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.h1
import org.w3c.dom.Document
import org.w3c.dom.Element

class BlogView(document: Document) : HasNavbarView(document) {
    override fun getTitle(): String {
        return "Blog da Loritta"
    }

    override fun generateContent(): Element {
        return document.create.div {
            for (i in 0 until 5) {
                h1 {
                    +"Um blog"
                }
            }
        }
    }
}