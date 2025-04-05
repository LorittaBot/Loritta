package net.perfectdreams.spicymorenitta.components

import net.perfectdreams.spicymorenitta.modals.Modal
import react.FC
import react.Props

external interface CloseModalButtonProps : Props {
    var modal: Modal
}

val CloseModalButton = FC<CloseModalButtonProps>("CloseModalButton") { props ->
    DiscordButtonReact {
        buttonType = DiscordButtonType.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT

        onClick = {
            println("Clicked close modal button")
            props.modal.close()
        }

        + "Fechar"
    }
}