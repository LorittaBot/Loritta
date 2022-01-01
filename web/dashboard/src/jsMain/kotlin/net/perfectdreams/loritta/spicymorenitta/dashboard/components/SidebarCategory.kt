package net.perfectdreams.loritta.spicymorenitta.dashboard.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun SidebarCategory(name: String, content: @Composable () -> Unit) {
    var showContent by remember { mutableStateOf(true) }

    Div(
        attrs = {
            classes("category")

            onClick {
                showContent = !showContent
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