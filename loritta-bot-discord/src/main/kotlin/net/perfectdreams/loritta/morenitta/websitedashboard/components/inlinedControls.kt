package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div

fun FlowContent.controlsWithButton(block: FlowContent.() -> Unit) {
    div(classes = "controls-with-button") {
        block()
    }
}

fun FlowContent.inlinedControls(block: FlowContent.() -> Unit) {
    div(classes = "inlined-controls") {
        block()
    }
}

fun FlowContent.growInputWrapper(block: FlowContent.() -> Unit) {
    div(classes = "grow-input") {
        block()
    }
}