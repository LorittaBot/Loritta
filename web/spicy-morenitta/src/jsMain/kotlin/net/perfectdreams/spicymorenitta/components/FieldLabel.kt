package net.perfectdreams.spicymorenitta.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Text

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