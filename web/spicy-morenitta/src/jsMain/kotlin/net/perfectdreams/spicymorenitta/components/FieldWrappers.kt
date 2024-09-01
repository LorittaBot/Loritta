package net.perfectdreams.spicymorenitta.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLDivElement

@Composable
fun FieldWrappers(attrs: AttrBuilderContext<HTMLDivElement>? = null, block: @Composable () -> (Unit)) {
    Div(
        attrs = {
            classes("field-wrappers")
            if (attrs != null) {
                attrs()
            }
        }
    ) {
        block.invoke()
    }
}