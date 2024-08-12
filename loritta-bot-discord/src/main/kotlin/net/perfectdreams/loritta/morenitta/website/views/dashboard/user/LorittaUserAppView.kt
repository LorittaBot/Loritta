package net.perfectdreams.loritta.morenitta.website.views.dashboard.user

import kotlinx.html.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.DiscordOAuth2AuthorizationURL
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.components.EtherealGambiUtils.etherealGambiImg
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession

class LorittaUserAppView(
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
    "user-app"
) {
    override fun DIV.generateRightSidebarContents() {
        div {
            div(classes = "hero-wrapper") {
                etherealGambiImg(
                    "https://stuff.loritta.website/pocket-loritta-itsgabi.png",
                    classes = "hero-image",
                    sizes = "(max-width: 900px) 100vw, 360px"
                ) {}

                div(classes = "hero-text") {
                    h1 {
                        text(i18nContext.get(I18nKeysData.Website.Dashboard.PocketLoritta.Title))
                    }

                    for (line in i18nContext.language
                        .textBundle
                        .lists
                        .getValue(I18nKeys.Website.Dashboard.PocketLoritta.Description.key)
                    ) {
                        p {
                            handleI18nString(
                                line,
                                appendAsFormattedText(i18nContext, mapOf()),
                            ) {
                                when (it) {
                                    "verifyMessageMention" -> {
                                        TextReplaceControls.ComposableFunctionResult {
                                            span(classes = "discord-mention") {
                                                text("/verificarmensagem")
                                            }
                                        }
                                    }

                                    else -> TextReplaceControls.AppendControlAsIsResult
                                }
                            }
                        }
                    }

                    val url = DiscordOAuth2AuthorizationURL {
                        append("client_id", lorittaWebsite.loritta.config.loritta.discord.applicationId.toString())
                        append("scope", "applications.commands")
                        append("integration_type", "1")
                    }

                    a(href = url.toString()) {
                        target = "_blank"
                        button(classes = "discord-button primary") {
                            text(i18nContext.get(I18nKeysData.Website.Dashboard.PocketLoritta.AddPocketLoritta))
                        }
                    }
                }
            }
        }
    }


    override fun getTitle() = locale["website.dailyShop.title"]
}