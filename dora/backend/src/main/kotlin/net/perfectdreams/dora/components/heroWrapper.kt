package net.perfectdreams.dora.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.img

fun FlowContent.heroWrapper(block: FlowContent.() -> (Unit)) {
    div(classes = "hero-wrapper") {
        block()
    }
}

fun FlowContent.simpleHeroImage(src: String) {
    img(src = src, classes = "hero-image")
}

fun FlowContent.heroText(block: FlowContent.() -> (Unit)) {
    div(classes = "hero-text") {
        block()
    }
}