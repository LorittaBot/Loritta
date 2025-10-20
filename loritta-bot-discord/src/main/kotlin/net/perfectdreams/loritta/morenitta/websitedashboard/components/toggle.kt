package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.checkBoxInput
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.label
import java.util.UUID

fun FlowContent.toggle(
    enabled: Boolean,
    checkboxName: String,
    trackCheckboxState: Boolean,
    title: FlowContent.() -> (Unit),
    description: (FlowContent.() -> (Unit))? = null
) {
    val toggleId = "toggle-${UUID.randomUUID()}"

    label(classes = "toggle-wrapper") {
        htmlFor = toggleId

        div(classes = "toggle-information") {
            div(classes = "toggle-title") {
                title()
            }

            if (description != null) {
                div(classes = "toggle-description") {
                    description()
                }
            }
        }

        div {
            checkBoxInput {
                if (trackCheckboxState)
                    attributes["loritta-config"] = checkboxName
                name = checkboxName
                if (enabled)
                    checked = true
                id = toggleId
            }

            div(classes = "switch-slider round") {}
        }
    }
}