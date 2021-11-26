package net.perfectdreams.loritta.spicymorenitta.dashboard.components.userdash

import SpicyMorenitta
import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.spicymorenitta.dashboard.components.UserOverviewContent
import net.perfectdreams.loritta.spicymorenitta.dashboard.screen.Screen
import net.perfectdreams.loritta.spicymorenitta.dashboard.styles.AppStylesheet
import org.jetbrains.compose.web.dom.Div

@Composable
fun UserOverview(
    m: SpicyMorenitta,
    screen: Screen.UserOverview
) {
    Div(attrs = { classes(AppStylesheet.wrapper) }) {
        UserLeftSidebar(m)

        Div(attrs = { classes(AppStylesheet.rightSidebar) }) {
            UserOverviewContent(screen)
        }

        Div(attrs = { classes(AppStylesheet.sidebarAd) }) {

        }
    }
}