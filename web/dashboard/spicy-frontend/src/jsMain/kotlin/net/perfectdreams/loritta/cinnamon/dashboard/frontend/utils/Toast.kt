package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

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