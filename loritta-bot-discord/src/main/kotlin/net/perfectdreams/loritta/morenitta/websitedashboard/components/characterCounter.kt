package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div

fun FlowContent.characterCounter(listenToElement: String) {
    div(classes = "character-counter") {
        attributes["bliss-component"] = "character-counter"
        attributes["character-counter-listen"] = listenToElement
    }
}