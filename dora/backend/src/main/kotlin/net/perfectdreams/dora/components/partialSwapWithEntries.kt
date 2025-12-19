package net.perfectdreams.dora.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.id

fun FlowContent.partialSwapWithEntries(
    entries: FlowContent.() -> Unit,
    body: FlowContent.() -> Unit
) {
    div(classes = "entries") {
        entries()
    }

    div {
        id = "right-sidebar-contents"

        body()
    }
}