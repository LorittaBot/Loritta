package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.id
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData

fun FlowContent.shipBuyButton(i18nContext: I18nContext, enabled: Boolean) {
    discordButton(ButtonStyle.SUCCESS) {
        attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/ship-effects/pre-buy"
        attributes["bliss-include-json"] = "[name=receivingEffectUserId], #ship-percentage"
        id = "buy-button"
        disabled = !enabled

        text(i18nContext.get(I18nKeysData.Website.Dashboard.PurchaseModal.Buy))
    }
}