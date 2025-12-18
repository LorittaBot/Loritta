package net.perfectdreams.luna.toasts

import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.script
import kotlinx.html.stream.createHTML
import kotlinx.serialization.json.Json
import net.perfectdreams.luna.bliss.BlissHex
import kotlin.collections.set

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