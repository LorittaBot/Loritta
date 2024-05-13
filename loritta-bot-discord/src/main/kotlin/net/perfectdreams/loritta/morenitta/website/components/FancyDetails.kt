package net.perfectdreams.loritta.morenitta.website.components

import kotlinx.html.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.ListI18nData
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData

object FancyDetails {
    fun FlowContent.fancyDetails(i18nContext: I18nContext, title: StringI18nData, description: ListI18nData) {
        fancyDetails(
            i18nContext.get(title),
            i18nContext.get(description)
        )
    }

    fun FlowContent.fancyDetails(title: String, description: List<String>) {
        details(classes = "fancy-details") {
            summary {
                text(title)

                div(classes = "chevron-icon") {
                    i(classes = "fa-solid fa-chevron-down") {}
                }
            }

            div(classes = "details-content") {
                for (line in description) {
                    p {
                        text(line)
                    }
                }
            }
        }
    }
}