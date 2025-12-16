package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.HTMLTag
import kotlinx.html.unsafe
import net.perfectdreams.loritta.morenitta.websitedashboard.svgicons.SVGIcon
import net.perfectdreams.loritta.morenitta.websitedashboard.svgicons.SVGIconManager
import org.jsoup.nodes.Element

fun HTMLTag.svgIcon(icon: SVGIcon, block: Element.() -> (Unit) = {}) {
    unsafe {
        val cloned = icon.html.clone()
        block(cloned)
        raw(cloned.toString())
    }
}