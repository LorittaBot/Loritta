package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.INPUT
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.label
import kotlinx.html.radioInput
import kotlinx.html.style
import java.util.UUID

fun FlowContent.fancyRadioInput(
    radioAttrs: INPUT.() -> (Unit),
    radioContent: FlowContent.() -> (Unit)
) {
    val radioId = "radio-${UUID.randomUUID()}"

    div(classes = "fancy-radio-option-wrapper") {
        radioInput {
            id = radioId
            style = "display: none;"

            radioAttrs()
        }

        label(classes = "fancy-radio-option") {
            htmlFor = radioId

            div(classes = "fancy-radio-option-circle") {
                div(classes = "fancy-radio-option-circle-white") {}
            }

            radioContent()
        }
    }
}