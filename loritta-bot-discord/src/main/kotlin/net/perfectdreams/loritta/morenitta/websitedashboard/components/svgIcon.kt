package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.HTMLTag
import kotlinx.html.unsafe
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons
import org.jsoup.nodes.Element

fun HTMLTag.svgIcon(icon: SVGIcons.SVGIcon, block: Element.() -> (Unit) = {}) {
    unsafe {
        val cloned = icon.html.clone()
        block(cloned)
        raw(cloned.toString())
    }
}