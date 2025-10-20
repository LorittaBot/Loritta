package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.checkBoxInput
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.hiddenInput
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.label
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import java.util.UUID

fun FlowContent.toggleableSection(
    title: (FlowContent.() -> (Unit)),
    description: (FlowContent.() -> (Unit))? = null,
    isOpen: Boolean,
    checkboxName: String,
    trackCheckboxState: Boolean,
    content: (FlowContent.() -> Unit)? = null,
) {
    div(classes = "toggleable-section") {
        if (isOpen) {
            classes += "is-open"
        }

        if (content != null) {
            classes += "section-content-not-empty"
        }

        attributes["bliss-component"] = "toggleable-section"

        div(classes = "toggleable-selection") {
            toggle(isOpen, checkboxName, trackCheckboxState, title, description)
        }

        if (content != null) {
            div(classes = "toggleable-content") {
                content()
            }
        }
    }
}