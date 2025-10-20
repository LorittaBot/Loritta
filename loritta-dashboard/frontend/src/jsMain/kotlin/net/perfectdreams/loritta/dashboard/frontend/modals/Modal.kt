package net.perfectdreams.loritta.dashboard.frontend.modals

import androidx.compose.runtime.Composable

class Modal(
    val modalManager: ModalManager,
    val title: String,
    val canBeClosedByClickingOutsideTheWindow: Boolean,
    val body: @Composable (Modal) -> (Unit),
    val buttons: List<@Composable (Modal) -> (Unit)>
) {
    fun close() {
        modalManager.closeModal(this)
    }
}