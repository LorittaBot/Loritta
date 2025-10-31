package net.perfectdreams.loritta.dashboard.frontend.compose.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Text
import web.html.HTMLDivElement

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

@Composable
fun FieldInformation(block: @Composable () -> (Unit)) {
    Div(
        attrs = {
            classes("field-information")
        }
    ) {
        block.invoke()
    }
}


@Composable
fun FieldLabel(text: String, forId: String) {
    Div(attrs = { classes("field-title") }) {
        Label(forId) {
            Text(text)
        }
    }
}

@Composable
fun FieldLabel(text: String) {
    Div(attrs = { classes("field-title") }) {
        Label {
            Text(text)
        }
    }
}