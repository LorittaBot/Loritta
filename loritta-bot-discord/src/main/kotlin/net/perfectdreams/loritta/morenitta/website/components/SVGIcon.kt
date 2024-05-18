package net.perfectdreams.loritta.morenitta.website.components

import kotlinx.html.FlowContent
import kotlinx.html.classes
import kotlinx.html.svg
import kotlinx.html.unsafe
import net.perfectdreams.loritta.morenitta.website.utils.SVGIconManager

object SVGIcon {
    fun FlowContent.svgIcon(icon: SVGIconManager.SVGIcon, classes: String) {
        val svgElement = icon.html.select("svg").first()

        svg {
            for (svgAttributes in svgElement.attributes()) {
                // kotlinx.html complains when adding a xmlns to a element
                if (svgAttributes.key != "xmlns") {
                    attributes[svgAttributes.key] = svgAttributes.value
                }
            }

            for (clazz in classes.split(" "))
                this.classes += clazz

            unsafe {
                raw(svgElement.html())
            }
        }
    }
}