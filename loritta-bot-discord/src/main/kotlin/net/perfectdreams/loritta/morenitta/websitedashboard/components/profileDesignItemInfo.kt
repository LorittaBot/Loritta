package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.img
import kotlinx.html.style
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.i18n.I18nKeysData

fun FlowContent.profileDesignItemInfo(
    i18nContext: I18nContext,
    locale: BaseLocale,
    profileDesignId: String,
    activeProfileDesignId: String,
    activeBackgroundId: String,
) {
    profileItemPreview(i18nContext, profileDesignId, activeBackgroundId)

    div {
        style = "text-align: center;"

        h1 {
            text(locale["profileDesigns.$profileDesignId.title"])
        }
    }

    div {
        text(locale["profileDesigns.$profileDesignId.description"])
    }

    itemInfoButtonsWrapper {
        discordButton(ButtonStyle.PRIMARY) {
            if (activeProfileDesignId == profileDesignId) {
                disabled = true
            } else {
                attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/profiles/${profileDesignId}"
                attributes["bliss-swap:200"] = "body (innerHTML) -> #trinket-info-content (innerHTML)"
            }

            text("Ativar")
        }
    }
}