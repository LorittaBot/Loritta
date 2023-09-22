package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

import androidx.compose.runtime.Composable

class Modal(
    val globalState: GlobalState,
    val title: String,
    val canBeClosedByClickingOutsideTheWindow: Boolean,
    val body: @Composable (Modal) -> (Unit),
    val buttons: List<@Composable (Modal) -> (Unit)>
) {
    fun close() {
        globalState.activeModals.remove(this)
    }
}