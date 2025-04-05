package net.perfectdreams.spicymorenitta.modals

import react.dom.html.HTMLAttributes
import web.html.HTMLDivElement

class Modal(
    val modalManager: ModalManager,
    val title: String,
    val canBeClosedByClickingOutsideTheWindow: Boolean,
    val body: HTMLAttributes<HTMLDivElement>.() -> (Unit),
    val buttons: List<HTMLAttributes<HTMLDivElement>.(Modal) -> (Unit)>
) {
    fun close() {
        modalManager.closeModal()
    }
}