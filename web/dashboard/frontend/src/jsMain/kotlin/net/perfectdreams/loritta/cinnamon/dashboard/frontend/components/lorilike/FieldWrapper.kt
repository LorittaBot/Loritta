package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Div

@Composable
fun FieldWrapper(block: @Composable () -> (Unit)) {
    Div(
        attrs = {
            classes("field-wrapper")
        }
    ) {
        block.invoke()
    }
}