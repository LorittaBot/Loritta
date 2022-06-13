package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun SidebarEntry(icon: SVGIconManager.SVGIcon, name: String) {
    Div(attrs = {
        classes("entry")
        attr("tabindex", "0") // Make the entry tabbable
    }) {
        UIIcon(icon) {
            classes("icon")
        }
        Div {
            Text(name)
        }
    }
}