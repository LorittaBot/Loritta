package net.perfectdreams.loritta.cinnamon.platform.modals.components

import net.perfectdreams.discordinteraktions.common.modals.components.ModalComponents
import net.perfectdreams.loritta.cinnamon.platform.modals.ModalSubmitExecutorDeclaration

class ModalComponentsWrapper(
    declarationExecutor: ModalSubmitExecutorDeclaration
) : ModalComponents() {
    init {
        // TODO: Fix this
        /* declarationExecutor.options.arguments.forEach {
            when (it) {
                is StringModalComponent -> textInput(it.name)
            }.register()
        } */
    }
}