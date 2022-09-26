package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.ContentBuilder
import org.w3c.dom.HTMLButtonElement

@Composable
fun DiscordButton(
    type: DiscordButtonType,
    attrs: AttrBuilderContext<HTMLButtonElement>? = null,
    content: ContentBuilder<HTMLButtonElement>? = null
) = Button({
    attrs?.invoke(this)

    classes(
        "discord-button",
        when (type) {
            DiscordButtonType.SUCCESS -> "success"
            DiscordButtonType.SECONDARY -> "secondary"
        }
    )
}, content)

enum class DiscordButtonType {
    SUCCESS,
    SECONDARY
}