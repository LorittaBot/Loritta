package net.perfectdreams.loritta.dashboard.frontend.compose.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.dashboard.frontend.utils.SVGIconManager
import web.svg.SVGElement

@Composable
fun SVGIcon(icon: SVGIconManager.SVGIcon, attrs: SVGElement.() -> (Unit) = {}) {
    val svg = icon.html.querySelector("svg")!!.cloneNode(true) as SVGElement
    attrs.invoke(svg)
    RawHtml(svg.outerHTML)
}