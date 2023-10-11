package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Svg
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.w3c.dom.asList
import org.w3c.dom.svg.SVGElement

@Composable
fun UIIcon(icon: SVGIconManager.SVGIcon, attrs: AttrBuilderContext<SVGElement>? = null) {
    Svg(
        {
            ref { element ->
                element.setAttribute("xmlns", "http://www.w3.org/2000/svg")
                // This is a STUPID HACKY WORKAROUND to fix the navbar icon in Safari
                // https://stackoverflow.com/questions/71022435/svg-invisible-on-safari-without-height-attribute-but-issue-not-recreatable
                element.setAttribute("height", "100%")
                icon.element.children.asList().forEach {
                    element.appendChild(it.cloneNode(true))
                }
                onDispose {}
            }

            val viewBox = icon.element.getAttribute("viewBox")
            if (viewBox != null)
                attr("viewBox", viewBox)

            attrs?.invoke(this)
        }
    )
}