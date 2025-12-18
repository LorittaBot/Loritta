package net.perfectdreams.luna.modals

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.ContentBuilder
import web.html.HTMLButtonElement

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
            DiscordButtonType.PRIMARY -> "primary"
            DiscordButtonType.SUCCESS -> "success"
            DiscordButtonType.SECONDARY -> "secondary"
            DiscordButtonType.DANGER -> "danger"
            DiscordButtonType.NO_BACKGROUND_LIGHT_TEXT -> "no-background-light-text"
            DiscordButtonType.NO_BACKGROUND_DARK_TEXT -> "no-background-dark-text"
            DiscordButtonType.NO_BACKGROUND_THEME_DEPENDENT_LIGHT_TEXT -> "no-background-theme-dependent-light-text"
            DiscordButtonType.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT -> "no-background-theme-dependent-dark-text"
        }
    )
}, content)

enum class DiscordButtonType {
    PRIMARY,
    SUCCESS,
    SECONDARY,
    DANGER,
    NO_BACKGROUND_LIGHT_TEXT,
    NO_BACKGROUND_DARK_TEXT,
    NO_BACKGROUND_THEME_DEPENDENT_LIGHT_TEXT,
    NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT,
}