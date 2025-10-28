package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.server.application.*
import kotlinx.html.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.DiscordOAuth2AuthorizationURL
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.EtherealGambiUtils.etherealGambiImg
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.userDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings

class PocketLorittaUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/user-app") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.PocketLoritta.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                null,
                {
                    userDashLeftSidebarEntries(website.loritta, i18nContext, userPremiumPlan, UserDashboardSection.POCKET_LORITTA)
                },
                {
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
                                append("client_id", website.loritta.config.loritta.discord.applicationId.toString())
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
            )
        }
    }
}