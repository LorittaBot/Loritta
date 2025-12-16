package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.IMG
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.img
import net.perfectdreams.loritta.morenitta.websitedashboard.svgicons.SVGIcon
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons
import org.jsoup.nodes.Element

fun FlowContent.simpleImageWithTextHeader(
    text: String,
    imageUrl: String,
    roundCorners: Boolean,
    block: IMG.() -> (Unit) = {}
) {
    div(classes = "simple-image-with-text-header") {
        img(src = imageUrl) {
            if (roundCorners)
                classes += "round-corners"

            block()
        }

        h1 {
            text(text)
        }
    }
}

fun FlowContent.simpleImageWithTextHeader(
    text: String,
    svgIcon: SVGIcon,
    block: Element.() -> (Unit) = {}
) {
    div(classes = "simple-image-with-text-header") {
        svgIcon(svgIcon, block)

        h1 {
            text(text)
        }
    }
}