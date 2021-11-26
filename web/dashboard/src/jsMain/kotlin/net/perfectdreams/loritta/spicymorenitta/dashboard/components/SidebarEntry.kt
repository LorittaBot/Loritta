package net.perfectdreams.loritta.spicymorenitta.dashboard.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.spicymorenitta.dashboard.styles.AppStylesheet
import net.perfectdreams.loritta.spicymorenitta.dashboard.utils.SVGIconManager
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun SidebarEntry(name: String) {
    A(attrs = { classes(AppStylesheet.leftSidebarEntry, AppStylesheet.resetLinkStyle) }) {
        UIIcon(SVGIconManager.sparkles) {
            classes(AppStylesheet.leftSidebarEntryIcon)
        }
        Div {
            Text(name)
        }
    }
}