package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div

fun FlowContent.cardsWithHeader(
    block: FlowContent.() -> (Unit)
) {
    div(classes = "cards-with-header") {
        block()
    }
}

fun FlowContent.cardHeader(
    block: FlowContent.() -> (Unit)
) {
    div(classes = "card-header") {
        block()
    }
}

fun FlowContent.cardHeaderInfo(
    block: FlowContent.() -> (Unit)
) {
    div(classes = "card-header-info") {
        block()
    }
}

fun FlowContent.cardHeaderTitle(
    block: FlowContent.() -> (Unit)
) {
    div(classes = "card-header-title") {
        block()
    }
}

fun FlowContent.cardHeaderDescription(
    block: FlowContent.() -> (Unit)
) {
    div(classes = "card-header-description") {
        block()
    }
}