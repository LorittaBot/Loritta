package net.perfectdreams.loritta.dashboard.frontend.toasts

import androidx.compose.runtime.Composable

data class Toast(
    val type: Type,
    val title: String,
    val body: @Composable () -> (Unit)
) {
    enum class Type {
        INFO,
        SUCCESS,
        WARN
    }
}