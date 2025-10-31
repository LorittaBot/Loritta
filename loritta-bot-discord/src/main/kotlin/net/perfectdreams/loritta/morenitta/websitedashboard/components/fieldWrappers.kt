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

fun FlowContent.fieldInformationWithControl(block: FlowContent.() -> (Unit)) {
    div(classes = "field-information-with-control") {
        block()
    }
}

fun FlowContent.fieldInformationBlock(block: FlowContent.() -> (Unit)) {
    div(classes = "field-information") {
        block()
    }
}

fun FlowContent.fieldInformation(title: String, description: String? = null) {
    fieldInformation(
        {
            text(title)
        },
        description?.let { { text(description) } }
    )
}

fun FlowContent.fieldInformation(title: FlowContent.() -> (Unit), description: (FlowContent.() -> (Unit))? = null) {
    div(classes = "field-information") {
        fieldTitle {
            title()
        }

        if (description != null) {
            fieldDescription {
                description()
            }
        }
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