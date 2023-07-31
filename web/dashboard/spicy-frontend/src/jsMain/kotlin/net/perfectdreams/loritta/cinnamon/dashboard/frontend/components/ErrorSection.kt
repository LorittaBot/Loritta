package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text

/**
 * A [ErrorSection] that fills the entire height of the parent div, centralizing the error section
 */
@Composable
fun FillContentErrorSection(text: String) {
    Div(attrs = { classes("error-section", "fill-content-error-section") }) {
        Img("https://assets.perfectdreams.media/loritta/emotes/lori-sob.png")

        Div {
            Text(text)
        }
    }
}