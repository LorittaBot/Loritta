package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.img
import kotlinx.html.style
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import java.util.UUID

fun FlowContent.badgeItemInfo(
    i18nContext: I18nContext,
    badgeId: UUID,
    title: StringI18nData,
    description: StringI18nData,
    isOwned: Boolean,
    isHidden: Boolean,
) {
    div {
        style = "text-align: center;"

        img {
            src = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/badge-image/$badgeId"
            style = if (isOwned) {
                "width: 128px; height: 128px;"
            } else {
                "width: 128px; height: 128px; filter: grayscale(1); opacity: 0.55;"
            }
        }

        h1 {
            text(i18nContext.get(title))
        }
    }

    div {
        text(i18nContext.get(description))
    }

    div {
        style = "margin-top: 1em; font-weight: bold;"

        if (isOwned) {
            text(i18nContext.get(DashboardI18nKeysData.Badges.Owned))
        } else {
            text(i18nContext.get(DashboardI18nKeysData.Badges.Locked))
        }
    }

    if (isOwned) {
        itemInfoButtonsWrapper {
            discordButton(ButtonStyle.PRIMARY) {
                attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/badges/$badgeId/toggle-visibility"
                attributes["bliss-swap:200"] = "body (innerHTML) -> #trinket-info-content (innerHTML)"
                attributes["bliss-indicator"] = "this"

                div {
                    if (isHidden) {
                        text(i18nContext.get(I18nKeysData.Commands.Command.Profilebadges.ShowBadgeInProfile))
                    } else {
                        text(i18nContext.get(I18nKeysData.Commands.Command.Profilebadges.HideBadgeInProfile))
                    }
                }

                div(classes = "loading-text-wrapper") {
                    text("Carregando...")
                }
            }
        }
    }
}
