package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalI18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPathWithArguments
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun SidebarEntryScreen(m: LorittaDashboardFrontend, icon: SVGIconManager.SVGIcon, name: String, screenPath: ScreenPathWithArguments) {
    AScreen(
        m,
        screenPath,
        {
            classes("entry")
        }
    ) {
        UIIcon(icon) {
            classes("icon")
        }
        Div {
            Text(name)
        }
    }
}

@Composable
fun SidebarEntryScreen(m: LorittaDashboardFrontend, icon: SVGIconManager.SVGIcon, name: StringI18nData, screenPath: ScreenPathWithArguments) = SidebarEntryScreen(
    m,
    icon,
    LocalI18nContext.current.get(name),
    screenPath
)