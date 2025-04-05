package net.perfectdreams.spicymorenitta.components

import react.FC
import react.PropsWithChildren
import react.dom.html.ReactHTML.button
import web.cssom.ClassName

external interface DiscordButtonReactProps : PropsWithChildren {
    var buttonType: DiscordButtonType
    var onClick: () -> (Unit)
}

val DiscordButtonReact = FC<DiscordButtonReactProps>("DiscordButtonReact") { props ->
    button {
        val classes = mutableListOf<String>("discord-button")

        classes.add(
            when (props.buttonType) {
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

        this.className = ClassName(classes.joinToString(" "))

        this.onClick = {
            props.onClick.invoke()
        }

        + props.children
    }
}