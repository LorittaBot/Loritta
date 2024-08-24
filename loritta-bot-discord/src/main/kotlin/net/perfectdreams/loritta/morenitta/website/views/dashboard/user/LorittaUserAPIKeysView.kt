package net.perfectdreams.loritta.morenitta.website.views.dashboard.user

import kotlinx.html.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession

class LorittaUserAPIKeysView(
    lorittaWebsite: LorittaWebsite,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    legacyBaseLocale: LegacyBaseLocale,
    userIdentification: LorittaJsonWebSession.UserIdentification,
    userPremiumPlan: UserPremiumPlans,
    colorTheme: ColorTheme
) : ProfileDashboardView(
    lorittaWebsite,
    i18nContext,
    locale,
    path,
    legacyBaseLocale,
    userIdentification,
    userPremiumPlan,
    colorTheme,
    "user-api-key"
) {
    override fun DIV.generateRightSidebarContents() {
        div {
            div(classes = "hero-wrapper") {
                /* etherealGambiImg(
                    "https://stuff.loritta.website/pocket-loritta-itsgabi.png",
                    classes = "hero-image",
                    sizes = "(max-width: 900px) 100vw, 360px"
                ) {} */

                div(classes = "hero-text") {
                    h1 {
                        text(i18nContext.get(I18nKeysData.Website.Dashboard.ApiKeys.Title))
                    }

                    for (line in i18nContext.get(I18nKeysData.Website.Dashboard.ApiKeys.Description)) {
                        p {
                            text(line)
                        }
                    }
                }
            }

            div(classes = "field-wrapper") {
                div(classes = "field-title") {
                    text(i18nContext.get(I18nKeysData.Website.Dashboard.ApiKeys.YourToken))
                }

                div {
                    code {
                        id = "user-api-key"
                        text(i18nContext.get(I18nKeysData.Website.Dashboard.ApiKeys.ResetTheTokenToGetIt))
                    }
                }
            }

            div {
                style = "color: red; font-weight: bold; color: #d32f2f;"
                text(i18nContext.get(I18nKeysData.Website.Dashboard.ApiKeys.DoNotShareYourAPIKey))
            }

            button(classes = "discord-button primary", type = ButtonType.button) {
                attributes["hx-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/dashboard/api-keys/generate"
                attributes["hx-target"] = "#user-api-key"
                text(i18nContext.get(I18nKeysData.Website.Dashboard.ApiKeys.GenerateNewToken))
            }
        }
    }

    override fun getTitle() = i18nContext.get(I18nKeysData.Website.Dashboard.ApiKeys.Title)
}