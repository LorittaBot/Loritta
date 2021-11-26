package net.perfectdreams.loritta.spicymorenitta.dashboard.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import net.perfectdreams.loritta.spicymorenitta.dashboard.styles.AppStylesheet

@Composable
fun SidebarCategory(name: String, content: @Composable () -> Unit) {
    var showContent by remember { mutableStateOf(true) }

    Div(
        attrs = {
            classes(AppStylesheet.leftSidebarCategory)

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