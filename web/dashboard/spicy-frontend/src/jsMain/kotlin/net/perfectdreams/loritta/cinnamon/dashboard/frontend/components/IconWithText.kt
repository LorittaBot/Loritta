package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import org.jetbrains.compose.web.dom.ContentBuilder
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLElement

@Composable
fun IconWithText(
    icon: SVGIconManager.SVGIcon,
    content: ContentBuilder<HTMLElement>
) {
    Div(
        attrs = {
            classes("icon-with-text")
        }
    ) {
        UIIcon(icon) {
            classes("icon")
        }
        content.invoke(this)
    }
}