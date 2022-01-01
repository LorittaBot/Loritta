package net.perfectdreams.loritta.spicymorenitta.dashboard.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.spicymorenitta.dashboard.utils.SVGIconManager
import net.perfectdreams.loritta.spicymorenitta.dashboard.utils.Svg
import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.TagElement
import org.w3c.dom.asList
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGImageElement

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