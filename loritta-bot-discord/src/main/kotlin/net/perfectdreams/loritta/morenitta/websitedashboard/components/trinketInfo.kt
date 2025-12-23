package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.id
import net.perfectdreams.i18nhelper.core.I18nContext

fun FlowContent.trinketInfo(i18nContext: I18nContext, content: FlowContent.() -> (Unit)) {
    div {
        id = "trinket-info"

        div {
            id = "trinket-info-content"
            content()
        }

        fillLoadingScreen(i18nContext)
    }
}