package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Div

@Composable
fun FieldWrappers(block: @Composable () -> (Unit)) {
    Div(
        attrs = {
            classes("field-wrappers")
        }
    ) {
        block.invoke()
    }
}