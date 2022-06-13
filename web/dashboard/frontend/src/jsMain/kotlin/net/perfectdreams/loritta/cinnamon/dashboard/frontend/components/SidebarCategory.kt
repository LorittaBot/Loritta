package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun SidebarCategory(name: String, content: @Composable () -> Unit) {
    val showContent by remember { mutableStateOf(true) }

    Div(
        attrs = {
            classes("category")

            onClick {
                // TODO: Maybe re-enable?
                // showContent = !showContent
            }
        }
    ) {
        Text(name)
    }

    if (showContent) {
        Div {
            content()
        }
    }
}