package net.perfectdreams.loritta.morenitta.website.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.img
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData

object EmptySection {
    fun FlowContent.emptySection(i18nContext: I18nContext) {
        div(classes = "empty-section") {
            img(src = "https://stuff.loritta.website/emotes/lori-sob.png") {
                width = "192"
                height = "192"
            }

            div(classes = "philosophical-text") {
                text(i18nContext.get(I18nKeysData.Commands.Command.Transactions.NoTransactionsFunnyMessages).random())
            }
        }
    }
}