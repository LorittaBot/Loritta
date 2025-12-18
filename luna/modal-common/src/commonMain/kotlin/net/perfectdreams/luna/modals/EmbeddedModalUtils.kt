package net.perfectdreams.luna.modals

import kotlinx.html.*
import kotlinx.html.stream.createHTML
import kotlinx.serialization.json.Json
import net.perfectdreams.luna.bliss.BlissHex
import kotlin.collections.set

/**
 * Creates an embedded modal
 */
fun createEmbeddedModal(
    title: String,
    size: EmbeddedModal.Size,
    canBeClosedByClickingOutsideTheWindow: Boolean,
    body: DIV.() -> (Unit),
    buttons: List<FlowContent.() -> (Unit)>
): EmbeddedModal {
    return EmbeddedModal(
        title,
        size,
        canBeClosedByClickingOutsideTheWindow,
        createHTML(false).div { body() },
        buttons.map { createHTML(false).span { it() } }
    )
}

/**
 * Adds a "show modal" to the DOM
 */
fun FlowContent.blissShowModal(modal: EmbeddedModal) {
    script(type = "application/json") {
        attributes["bliss-show-modal"] = "true"
        attributes["bliss-modal"] = BlissHex.encodeToHexString(Json.encodeToString(modal))
    }
}

/**
 * Adds a "close modal" to the DOM
 */
fun FlowContent.blissCloseModal() {
    script(type = "application/json") {
        attributes["bliss-close-modal"] = "true"
    }
}

/**
 * Adds a "close all modals" to the DOM
 */
fun FlowContent.blissCloseAllModals() {
    script(type = "application/json") {
        attributes["bliss-close-all-modals"] = "true"
    }
}

/**
 * Opens a modal on click
 */
fun FlowContent.openModalOnClick(modal: EmbeddedModal) {
    attributes["bliss-modal"] = BlissHex.encodeToHexString(Json.encodeToString(modal))
    attributes["bliss-open-modal-on-click"] = "true"
}