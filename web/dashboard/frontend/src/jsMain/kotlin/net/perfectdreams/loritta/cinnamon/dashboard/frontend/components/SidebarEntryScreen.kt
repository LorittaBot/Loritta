package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.Screen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalI18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun SidebarEntryScreen(m: LorittaDashboardFrontend, icon: SVGIconManager.SVGIcon, name: String, screen: () -> (Screen)) {
    A(
        attrs = {
            classes("entry")
            attr("tabindex", "0") // Make the entry tabbable

            onClick {
                it.preventDefault()

                m.routingManager.switch(screen.invoke())
            }
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
fun SidebarEntryScreen(m: LorittaDashboardFrontend, icon: SVGIconManager.SVGIcon, name: StringI18nData, screen: () -> (Screen)) = SidebarEntryScreen(
    m,
    icon,
    LocalI18nContext.current.get(name),
    screen
)