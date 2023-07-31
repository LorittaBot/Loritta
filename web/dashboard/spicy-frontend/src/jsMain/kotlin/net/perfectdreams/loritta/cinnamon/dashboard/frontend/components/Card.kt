package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Div

@Composable
fun Card(block: @Composable () -> (Unit) = {}) {
    Div(
        attrs = {
            classes("card")
        }
    ) {
        block()
    }
}