package net.perfectdreams.loritta.morenitta.websitedashboard.utils

import kotlinx.html.*
import kotlin.collections.set

/**
 * Adds a "trigger event" to the DOM
 */
fun FlowContent.blissSoundEffect(soundEffectName: String) {
    script(type = "application/json") {
        attributes["bliss-sound-effect"] = soundEffectName
    }
}