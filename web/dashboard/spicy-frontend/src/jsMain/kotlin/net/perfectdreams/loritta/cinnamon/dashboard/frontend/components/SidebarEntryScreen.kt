package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalI18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPath
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun SidebarEntryScreen(m: LorittaDashboardFrontend, icon: SVGIconManager.SVGIcon, name: String, screenPath: ScreenPath) {
    val i18nContext = LocalI18nContext.current
    A(
        href = "/${i18nContext.get(I18nKeysData.Website.Dashboard.LocalePathId)}${screenPath.build()}",
        attrs = {
            classes("entry")
            attr("tabindex", "0") // Make the entry tabbable

            onClick {
                it.preventDefault()

                m.routingManager.switchBasedOnPath(i18nContext, screenPath.build(), false)
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
fun SidebarEntryScreen(m: LorittaDashboardFrontend, icon: SVGIconManager.SVGIcon, name: StringI18nData, screenPath: ScreenPath) = SidebarEntryScreen(
    m,
    icon,
    LocalI18nContext.current.get(name),
    screenPath
)