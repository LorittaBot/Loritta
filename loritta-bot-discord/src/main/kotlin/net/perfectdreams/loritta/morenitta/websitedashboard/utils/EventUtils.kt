package net.perfectdreams.loritta.morenitta.websitedashboard.utils

import kotlinx.html.*
import kotlin.collections.set

/**
 * Adds a "trigger event" to the DOM
 */
fun FlowContent.blissEvent(eventName: String, eventTarget: String) {
    script(type = "application/json") {
        attributes["bliss-event"] = eventName
        attributes["bliss-event-target"] = eventTarget
    }
}