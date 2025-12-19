package net.perfectdreams.dora.utils

import kotlinx.html.BUTTON
import net.perfectdreams.dora.components.ButtonStyle
import net.perfectdreams.dora.components.discordButton
import net.perfectdreams.luna.modals.EmbeddedModal
import net.perfectdreams.luna.modals.closeModalOnClick
import net.perfectdreams.luna.modals.createEmbeddedModal

fun createEmbeddedConfirmDeletionModal(
    confirmDeletionButtonBehavior: BUTTON.() -> (Unit)
): EmbeddedModal {
    return createEmbeddedModal(
        "Você tem certeza?",
        EmbeddedModal.Size.SMALL,
        true,
        {
            text("Você quer deletar meeesmo?")
        },
        listOf(
            {
                discordButton(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT) {
                    closeModalOnClick()

                    text("Fechar")
                }
            },
            {
                discordButton(ButtonStyle.DANGER) {
                    confirmDeletionButtonBehavior.invoke(this)

                    text("Excluir")
                }
            }
        )
    )
}