package net.perfectdreams.spicymorenitta.components.messages

import androidx.compose.runtime.Composable
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.attributes.ButtonType
import org.jetbrains.compose.web.attributes.type
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.ContentBuilder
import org.jetbrains.compose.web.dom.Div
import web.html.HTMLButtonElement
import web.html.HTMLDivElement

@Composable
fun DiscordButton(
    type: DiscordButtonType,
    attrs: AttrBuilderContext<HTMLButtonElement>? = null,
    content: ContentBuilder<HTMLButtonElement>? = null
) = Button({
    this.type(ButtonType.Button)
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

@Composable
fun TextWithIconWrapper(
    // TODO: Fix this!
    // icon: SVGIconManager.SVGIcon,
    // svgAttrs: AttrsScope<SVGElement>.() -> (Unit),
    content: ContentBuilder<HTMLDivElement>? = null
) = Div({
    classes("text-with-icon-wrapper")
}) {
    // TODO: Fix this
    /* UIIcon(icon) {
        classes("text-icon")
        svgAttrs()
    } */

    Div(content = content)
}

fun AttrsScope<HTMLButtonElement>.disabledWithSoundEffect() {
    classes("disabled")
    attr("aria-disabled", "true")

    onClick {
        SpicyMorenitta.INSTANCE.soundEffects.error.play(1.0)
    }
}

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