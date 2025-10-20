package net.perfectdreams.loritta.dashboard.frontend.compose.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import org.jetbrains.compose.web.dom.Span
import web.dom.document

@Composable
fun RawHtml(rawHtml: String) {
    // We need to do this because Compose doesn't really know when the rawHtml actually IS different
    // (Example on the select menu: if the text of the element is HtmlText, and you change the entry, it keeps the old text
    // because it thinks "well it is the same HtmlText so why rerender?")
    key(rawHtml) {
        Span(attrs = {
            ref { element ->
                // Required to execute inline scripts
                // https://pierodetomi.medium.com/how-to-append-dynamic-html-with-scripts-to-the-dom-14509c1ca784
                val range = document.createRange()
                val fragment = range.createContextualFragment(rawHtml)
                element.appendChild(fragment)

                onDispose {}
            }
        })
    }
}