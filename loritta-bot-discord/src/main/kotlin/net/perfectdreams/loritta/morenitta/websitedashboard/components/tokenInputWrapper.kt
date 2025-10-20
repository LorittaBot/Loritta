package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.ButtonType
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.InputType
import kotlinx.html.button
import kotlinx.html.id
import kotlinx.html.input
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData

fun FlowContent.tokenInputWrapper(i18nContext: I18nContext, apiToken: String?) {
    val inputType = if (apiToken == null) InputType.password else InputType.text
    val tokenValue = apiToken ?: "ParabensVocêEncontrouUmEasterEgg!!!ALoriÉMuitoFofa"

    input(inputType) {
        id = "user-api-key"
        value = tokenValue
        readonly = true
    }

    button(classes = "discord-button success", type = ButtonType.button) {
        if (apiToken != null) {
            attributes["bliss-copy-text-on-click"] = apiToken
        } else disabled = true

        text(i18nContext.get(I18nKeysData.Website.Dashboard.ApiKeys.CopyTokenButton))
    }
}