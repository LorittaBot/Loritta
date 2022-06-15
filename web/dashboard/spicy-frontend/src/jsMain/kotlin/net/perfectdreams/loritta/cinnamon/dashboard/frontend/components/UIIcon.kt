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
                val viewBox = icon.element.getAttribute("viewBox")
                if (viewBox != null)
                    element.setAttributeNS(null, "viewBox", viewBox)

                icon.element.children.asList().forEach {
                    element.appendChild(it.cloneNode(true))
                }
                onDispose {}
            }

            attrs?.invoke(this)
        }
    )
}