package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.id

fun FlowContent.trinketInfo(content: FlowContent.() -> (Unit)) {
    div {
        id = "trinket-info"

        div {
            id = "trinket-info-content"
            content()
        }

        fillLoadingScreen()
    }
}