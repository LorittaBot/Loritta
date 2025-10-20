package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div

fun FlowContent.fieldWrappers(block: FlowContent.() -> (Unit)) {
    div(classes = "field-wrappers") {
        block()
    }
}

fun FlowContent.fieldWrapper(block: DIV.() -> (Unit)) {
    div(classes = "field-wrapper") {
        block()
    }
}

fun FlowContent.fieldTitle(block: FlowContent.() -> (Unit)) {
    div(classes = "field-title") {
        block()
    }
}

fun FlowContent.fieldDescription(block: FlowContent.() -> (Unit)) {
    div(classes = "field-description") {
        block()
    }
}