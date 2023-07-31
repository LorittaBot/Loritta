package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.common.utils.Color
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun InlineDiscordMention(
    text: String,
    color: Color? = null
) {
    Span(attrs = {
        classes("discord-mention")

        if (color != null) {
            style {
                color(rgb(color.red, color.green, color.blue))
                backgroundColor(rgba(color.red, color.green, color.blue, 0.1))
            }
        }
    }) {
        Text("@")
        Text(text)
    }
}