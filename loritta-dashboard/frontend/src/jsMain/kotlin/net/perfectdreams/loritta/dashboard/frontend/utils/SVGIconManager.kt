package net.perfectdreams.loritta.dashboard.frontend.utils

import web.dom.Document
import web.parsing.DOMParser
import web.parsing.DOMParserSupportedType
import web.parsing.imageSvgXml

object SVGIconManager {
    fun fromRawHtml(html: String): SVGIcon {
        val parser = DOMParser()
        val document = parser.parseFromString(html, DOMParserSupportedType.imageSvgXml)
        return SVGIcon(document)
    }

    class SVGIcon(val html: Document) {
        val rawHtml = html.querySelector("svg")!!.outerHTML
    }
}