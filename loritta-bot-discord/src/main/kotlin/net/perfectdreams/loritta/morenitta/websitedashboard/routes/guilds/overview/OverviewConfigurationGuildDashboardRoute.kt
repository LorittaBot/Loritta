package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.overview

import io.ktor.server.application.*
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.style
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroText
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.simpleHeroImage
import net.perfectdreams.loritta.morenitta.websitedashboard.components.svgIcon
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings

class OverviewConfigurationGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/overview") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.Overview.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.OVERVIEW)
                },
                {
                    heroWrapper {
                        simpleHeroImage("https://stuff.loritta.website/animations/loritta-dashboard/loritta-dashboard.png")
                        heroText {
                            h1 {
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.Overview.Title))
                            }

                            p(classes = "computer-description") {
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.Overview.DescriptionComputer))
                            }

                            p(classes = "mobile-description") {
                                handleI18nString(
                                    i18nContext,
                                    I18nKeys.Website.Dashboard.Overview.DescriptionMobile,
                                    appendAsFormattedText(i18nContext, mapOf()),
                                    {
                                        when (it) {
                                            "hamburgerButton" -> {
                                                TextReplaceControls.ComposableFunctionResult {
                                                    span {
                                                        svgIcon(SVGIcons.List) {
                                                            attr("style", "width: 1em; height: 1em; vertical-align: middle;")
                                                        }
                                                    }
                                                }
                                            }
                                            else -> TextReplaceControls.AppendControlAsIsResult
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}