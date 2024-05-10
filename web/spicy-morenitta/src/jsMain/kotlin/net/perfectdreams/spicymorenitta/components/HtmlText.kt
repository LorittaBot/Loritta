package net.perfectdreams.spicymorenitta.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Span

@Composable
fun HtmlText(rawHtml: String) {
    Span(attrs = {
        ref { element ->
            element.innerHTML = rawHtml
            onDispose {}
        }
    })
}