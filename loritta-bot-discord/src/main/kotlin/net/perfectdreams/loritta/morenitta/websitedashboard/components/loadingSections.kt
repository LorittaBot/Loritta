package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.IMG
import kotlinx.html.div
import kotlinx.html.img
import net.perfectdreams.loritta.morenitta.website.components.LoadingSectionComponents

fun FlowContent.loadingSpinnerImage(block: IMG.() -> (Unit) = {}) {
    img {
        src = LoadingSectionComponents.list.random()
        attributes["bliss-component"] = "rotating-image"
        attributes["rotating-image-urls"] = LoadingSectionComponents.list.joinToString(",")

        block()
    }
}

fun FlowContent.fillLoadingScreen() {
    div(classes = "fill-loading-screen") {
        loadingSpinnerImage()

        text("Carregando...")
    }
}