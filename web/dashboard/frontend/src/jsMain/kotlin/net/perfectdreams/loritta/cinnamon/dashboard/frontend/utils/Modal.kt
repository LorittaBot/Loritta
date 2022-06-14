package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

import androidx.compose.runtime.Composable

data class Modal(
    val body: @Composable () -> (Unit),
    val buttons: List<@Composable () -> (Unit)>
)