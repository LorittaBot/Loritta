package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.button
import kotlinx.html.details
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.style
import kotlinx.html.summary
import kotlinx.html.textInput
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData

fun FlowContent.fancyDetails(
    summary: FlowContent.() -> (Unit),
    content: FlowContent.() -> (Unit)
) {
    details(classes = "fancy-details") {
        summary {
            summary()

            div(classes = "chevron-icon") {
                // TODO: Add the chevron icon here!
            }
        }

        div(classes = "details-content") {
            content()
        }
    }
}