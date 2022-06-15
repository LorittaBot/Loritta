package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.UIIcon
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import org.jetbrains.compose.web.dom.A
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

@Composable
fun SidebarEntryLink(icon: SVGIconManager.SVGIcon, href: String, name: String) {
    A(
        href = href,
        attrs = {
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