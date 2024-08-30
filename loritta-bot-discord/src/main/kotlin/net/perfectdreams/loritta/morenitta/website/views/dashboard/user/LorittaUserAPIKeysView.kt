package net.perfectdreams.loritta.morenitta.website.views.dashboard.user

import kotlinx.html.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
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

                    for (str in i18nContext.language
                        .textBundle
                        .lists
                        .getValue(I18nKeys.Website.Dashboard.ApiKeys.Description.key)
                    ) {
                        p {
                            handleI18nString(
                                str,
                                appendAsFormattedText(i18nContext, mapOf()),
                            ) {
                                when (it) {
                                    "apiDocs" -> {
                                        TextReplaceControls.ComposableFunctionResult {
                                            a(href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/developers/docs") {
                                                attributes["hx-select"] = "#wrapper"
                                                attributes["hx-target"] = "#wrapper"
                                                attributes["hx-get"] = "/${locale.path}/developers/docs"
                                                attributes["hx-indicator"] = "#right-sidebar-wrapper"
                                                attributes["hx-push-url"] = "true"
                                                // show:top - Scroll to the top
                                                // settle:0ms - We don't want the settle animation beccause it is a full page swap
                                                // swap:0ms - We don't want the swap animation because it is a full page swap
                                                attributes["hx-swap"] = "outerHTML show:top settle:0ms swap:0ms"
                                                text(i18nContext.get(I18nKeysData.Website.Dashboard.ApiKeys.ApiDocs))
                                            }
                                        }
                                    }

                                    else -> TextReplaceControls.AppendControlAsIsResult
                                }
                            }
                        }
                    }
                }
            }

            div(classes = "field-wrapper") {
                div(classes = "field-title") {
                    text(i18nContext.get(I18nKeysData.Website.Dashboard.ApiKeys.YourToken))
                }

                div {
                    text(i18nContext.get(I18nKeysData.Website.Dashboard.ApiKeys.ResetTheTokenToGetIt))
                }

                div(classes = "alert alert-danger") {
                    text(i18nContext.get(I18nKeysData.Website.Dashboard.ApiKeys.DoNotShareYourAPIKey))
                }

                div {
                    id = "user-api-key-wrapper"
                    style = "display: flex; gap: 0.5em;"

                    tokenInputWrapper(i18nContext, null)
                }
            }

            button(classes = "discord-button primary", type = ButtonType.button) {
                style = "margin-top: 0.25em;"
                attributes["hx-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/dashboard/api-keys/generate"
                attributes["hx-target"] = "#user-api-key-wrapper"
                attributes["hx-swap"] = "outerHTML"
                attributes["hx-disabled-elt"] = "this"
                text(i18nContext.get(I18nKeysData.Website.Dashboard.ApiKeys.GenerateNewToken))
            }
        }
    }

    override fun getTitle() = i18nContext.get(I18nKeysData.Website.Dashboard.ApiKeys.Title)

    companion object {
        fun DIV.tokenInputWrapper(i18nContext: I18nContext, apiToken: String?) {
            val inputType = if (apiToken == null) InputType.password else InputType.text
            val tokenValue = apiToken ?: "ParabensVocêEncontrouUmEasterEgg!!!ALoriÉMuitoFofa"

            input(inputType) {
                id = "user-api-key"
                value = tokenValue
                readonly = true
            }

            button(classes = "discord-button success", type = ButtonType.button) {
                if (apiToken != null) {
                    script {
                        unsafe {
                            raw("""
                                me().on("click", function() {
                                    navigator.clipboard.writeText("$apiToken")
                                })
                            """.trimIndent())
                        }
                    }
                } else disabled = true

                text(i18nContext.get(I18nKeysData.Website.Dashboard.ApiKeys.CopyTokenButton))
            }
        }
    }
}