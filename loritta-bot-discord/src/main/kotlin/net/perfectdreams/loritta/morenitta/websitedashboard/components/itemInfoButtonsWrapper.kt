package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div

fun FlowContent.itemInfoButtonsWrapper(block: FlowContent.() -> Unit) {
    div(classes = "items-info-buttons-wrapper") {
        block()
    }
}