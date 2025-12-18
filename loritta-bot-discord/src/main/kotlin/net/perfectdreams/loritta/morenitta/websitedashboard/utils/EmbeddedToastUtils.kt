package net.perfectdreams.loritta.morenitta.websitedashboard.utils

import kotlinx.html.*
import kotlinx.html.div
import kotlinx.html.stream.createHTML
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.dashboard.BlissHex
import net.perfectdreams.luna.toasts.EmbeddedToast

/**
 * Creates an embedded toast
 */
fun createEmbeddedToast(
    type: EmbeddedToast.Type,
    title: String,
    description: (DIV.() -> (Unit))? = null
): EmbeddedToast {
    return EmbeddedToast(
        type,
        title,
        if (description == null) null else createHTML(false).div { description() }
    )
}

/**
 * Adds a "show toast" to the DOM
 */
fun FlowContent.blissShowToast(modal: EmbeddedToast) {
    script(type = "application/json") {
        attributes["bliss-show-toast"] = "true"
        attributes["bliss-toast"] = BlissHex.encodeToHexString(Json.encodeToString(modal))
    }
}