package net.perfectdreams.loritta.spicymorenitta.dashboard.components.userdash

import SpicyMorenitta
import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.spicymorenitta.dashboard.components.UserOverviewContent
import net.perfectdreams.loritta.spicymorenitta.dashboard.components.ads.nitropay.NitroPayAd
import net.perfectdreams.loritta.spicymorenitta.dashboard.screen.Screen
import org.jetbrains.compose.web.dom.Aside
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Section

@Composable
fun UserOverview(
    m: SpicyMorenitta,
    screen: Screen.UserOverview
) {
    Div(attrs = { id("wrapper") }) {
        UserLeftSidebar(m)

        Section(attrs = { id("right-sidebar") }) {
            UserOverviewContent(m, screen)
        }

        Aside(attrs = { id("sidebar-ad") }) {
            // TODO: Change Ad ID
            NitroPayAd(m, "nitropay-test-ad2", listOf("160x600"))
        }
    }
}