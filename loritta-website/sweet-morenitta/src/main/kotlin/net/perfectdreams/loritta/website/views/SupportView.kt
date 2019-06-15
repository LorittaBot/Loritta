package net.perfectdreams.loritta.website.views

import kotlinx.html.dom.create
import kotlinx.html.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

class SupportView(document: Document) : HasNavbarView(document) {
    override fun getTitle(): String {
        return "Suporte!"
    }

    override fun generateContent(): Element {
        return document.create.div {
            for (i in 0 until 5) {
                h1 {
                    +"Links para suporte aqui"
                }
            }

            img(src = "https://loritta.website/assets/img/fanarts/l7.png")
        }
    }
}