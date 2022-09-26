package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Article
import org.jetbrains.compose.web.dom.Section

@Composable
fun RightSidebar(block: @Composable () -> (Unit)) {
    Section(attrs = { id("right-sidebar") }) {
        Article(attrs = { classes("content") }) {
            block.invoke()
        }
    }
}