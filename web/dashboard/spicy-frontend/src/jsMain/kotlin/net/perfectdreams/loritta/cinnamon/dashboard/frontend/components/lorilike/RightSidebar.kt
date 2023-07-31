package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import org.jetbrains.compose.web.dom.Article
import org.jetbrains.compose.web.dom.Section

@Composable
fun RightSidebar(m: LorittaDashboardFrontend, block: @Composable () -> (Unit)) {
    Section(attrs = { id("right-sidebar") }) {
        Article(attrs = { classes("content") }) {
            block.invoke()
        }

        AdSidebar(m)
    }
}