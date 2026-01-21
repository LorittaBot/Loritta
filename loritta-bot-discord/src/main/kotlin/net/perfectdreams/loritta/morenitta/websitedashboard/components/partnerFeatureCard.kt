package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import net.perfectdreams.loritta.morenitta.websitedashboard.svgicons.SVGIcon

fun FlowContent.partnerFeatureCard(icon: SVGIcon, title: String, description: String) {
    div(classes = "partner-feature-card") {
        div(classes = "partner-feature-icon") {
            svgIcon(icon)
        }

        div(classes = "partner-feature-title") {
            text(title)
        }

        div(classes = "partner-feature-description") {
            text(description)
        }
    }
}