package net.perfectdreams.spicymorenitta.components.messages

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.common.utils.Color
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.rgb
import org.jetbrains.compose.web.css.rgba
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
        Text(text)
    }
}