package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLDivElement

@Composable
fun Card(attrs: AttrBuilderContext<HTMLDivElement>? = null, block: @Composable () -> (Unit) = {}) {
    Div(
        attrs = {
            attrs?.invoke(this)
            classes("card")
        }
    ) {
        block()
    }
}