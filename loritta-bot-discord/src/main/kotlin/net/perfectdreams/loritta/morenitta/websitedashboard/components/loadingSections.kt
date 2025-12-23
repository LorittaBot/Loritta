package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.IMG
import kotlinx.html.div
import kotlinx.html.img
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.website.components.LoadingSectionComponents
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData

fun FlowContent.loadingSpinnerImage(block: IMG.() -> (Unit) = {}) {
    img {
        src = LoadingSectionComponents.list.random()
        attributes["bliss-component"] = "rotating-image"
        attributes["rotating-image-urls"] = LoadingSectionComponents.list.joinToString(",")

        block()
    }
}

fun FlowContent.fillLoadingScreen(i18nContext: I18nContext) {
    div(classes = "fill-loading-screen") {
        loadingSpinnerImage()

        text(i18nContext.get(DashboardI18nKeysData.Loading))
    }
}