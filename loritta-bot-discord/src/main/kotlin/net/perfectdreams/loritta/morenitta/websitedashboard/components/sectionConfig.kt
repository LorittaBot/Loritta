package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.id

fun FlowContent.sectionConfig(block: FlowContent.() -> (Unit)) {
    div {
        id = "section-config"

        block()
    }
}