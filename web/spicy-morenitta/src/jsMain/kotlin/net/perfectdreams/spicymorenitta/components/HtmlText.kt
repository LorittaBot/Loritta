package net.perfectdreams.spicymorenitta.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import org.jetbrains.compose.web.dom.Span

@Composable
fun HtmlText(rawHtml: String) {
    // We need to do this because Compose doesn't really know when the rawHtml actually IS different
    // (Example on the select menu: if the text of the element is HtmlText, and you change the entry, it keeps the old text
    // because it thinks "well it is the same HtmlText so why rerender?")
    key(rawHtml) {
        Span(attrs = {
            ref { element ->
                element.innerHTML = rawHtml
                onDispose {}
            }
        })
    }
}