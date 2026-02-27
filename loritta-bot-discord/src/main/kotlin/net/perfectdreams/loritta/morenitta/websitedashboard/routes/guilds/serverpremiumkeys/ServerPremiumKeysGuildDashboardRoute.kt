package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.serverpremiumkeys

import io.ktor.server.application.*
import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlan
import net.perfectdreams.loritta.common.utils.UserPremiumPlan
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.PremiumPlanColumn
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.paymentHeroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.premiumBillingToggle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.premiumFeatureRow
import net.perfectdreams.loritta.morenitta.websitedashboard.components.premiumFeatureTableHead
import net.perfectdreams.loritta.morenitta.websitedashboard.components.premiumPlanCards
import net.perfectdreams.loritta.morenitta.websitedashboard.components.premiumValueRow
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme

class ServerPremiumKeysGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/premium") {
    data class PlanColumn(
        val name: String,
        val plan: ServerPremiumPlan,
        val planValue: Double,
        val monthlyPriceCents: Long,
        val highlight: Boolean
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlan, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlan, member: Member) {
        val plans = listOf(
            PlanColumn(i18nContext.get(DashboardI18nKeysData.PremiumKeys.PlanNames.Basic), ServerPremiumPlan.Basic, 34.99, 3499L, false),
            PlanColumn(i18nContext.get(DashboardI18nKeysData.PremiumKeys.PlanNames.Complete), ServerPremiumPlan.Complete, 59.99, 5999L, true)
        )

        val tablePlans = listOf(
            PlanColumn(i18nContext.get(DashboardI18nKeysData.PremiumKeys.PlanNames.Free), ServerPremiumPlan.Free, 0.0, 0L, false),
            PlanColumn(i18nContext.get(DashboardI18nKeysData.PremiumKeys.PlanNames.Basic), ServerPremiumPlan.Basic, 34.99, 3499L, false),
            PlanColumn(i18nContext.get(DashboardI18nKeysData.PremiumKeys.PlanNames.Complete), ServerPremiumPlan.Complete, 59.99, 5999L, true)
        )

        val localePath = i18nContext.get(net.perfectdreams.loritta.i18n.I18nKeysData.Website.LocalePathId)
        val pageTitle = i18nContext.get(DashboardI18nKeysData.PremiumKeys.ServerPremium.PageTitle)

        call.respondHtml {
            dashboardBase(
                i18nContext,
                pageTitle,
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.SERVER_PREMIUM_KEYS)
                },
                {
                    div {
                        style = "text-align: center;"

                        paymentHeroWrapper(i18nContext)

                        hr {}

                        h1 {
                            text(pageTitle)
                        }
                    }

                    if (guildPremiumPlan != ServerPremiumPlan.Free) {
                        div {
                            style = "background: var(--loritta-blue); color: white; padding: 1em; border-radius: 8px; margin-bottom: 1em; text-align: center;"

                            p {
                                text(i18nContext.get(DashboardI18nKeysData.PremiumKeys.ServerPremium.CurrentPlan))
                                b {
                                    text(
                                        when (guildPremiumPlan) {
                                            ServerPremiumPlan.Basic -> i18nContext.get(DashboardI18nKeysData.PremiumKeys.PlanNames.Basic)
                                            ServerPremiumPlan.Complete -> i18nContext.get(DashboardI18nKeysData.PremiumKeys.PlanNames.Complete)
                                            else -> i18nContext.get(DashboardI18nKeysData.PremiumKeys.PlanNames.Unknown)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Premium billing container (radio inputs, toggle, and plan cards as siblings for CSS-only switching)
                    div(classes = "premium-billing-container") {
                        premiumBillingToggle(i18nContext)

                        premiumPlanCards(
                            i18nContext,
                            plans.map { PremiumPlanColumn(it.name, it.planValue, it.monthlyPriceCents, it.highlight) },
                            "/$localePath/guilds/${guild.idLong}/premium/buy"
                        )
                    }

                    div {
                        style = "overflow-x: auto;"

                        table {
                            style = "width: 100%; border-collapse: collapse; text-align: center;"

                            premiumFeatureTableHead(
                                i18nContext,
                                tablePlans.map { PremiumPlanColumn(it.name, it.planValue, it.monthlyPriceCents, it.highlight) }
                            )

                            tbody {
                                premiumValueRow(i18nContext.get(DashboardI18nKeysData.PremiumKeys.ServerPremium.Features.YoutubeAndTwitchChannels), tablePlans, { it.highlight }) { "${it.plan.maxYouTubeChannels}" }
                                premiumValueRow(i18nContext.get(DashboardI18nKeysData.PremiumKeys.ServerPremium.Features.PremiumTwitchTracking), tablePlans, { it.highlight }) { "${it.plan.maxUnauthorizedTwitchChannels}" }
                                premiumValueRow(i18nContext.get(DashboardI18nKeysData.PremiumKeys.ServerPremium.Features.MemberCounterChannels), tablePlans, { it.highlight }) { "${it.plan.memberCounterCount}" }
                                premiumFeatureRow(i18nContext.get(DashboardI18nKeysData.PremiumKeys.ServerPremium.Features.ExclusiveBadge), tablePlans, { it.highlight }) { it.plan.hasCustomBadge }
                                premiumValueRow(i18nContext.get(DashboardI18nKeysData.PremiumKeys.ServerPremium.Features.MaxLevelUpRoles), tablePlans, { it.highlight }) { "${it.plan.maxLevelUpRoles}" }
                                premiumValueRow(i18nContext.get(DashboardI18nKeysData.PremiumKeys.ServerPremium.Features.DailyMultiplier), tablePlans, { it.highlight }) { "${it.plan.dailyMultiplier}x" }
                                premiumFeatureRow(i18nContext.get(DashboardI18nKeysData.PremiumKeys.ServerPremium.Features.DropGuildInfo), tablePlans, { it.highlight }) { it.plan.showDropGuildInfoOnTransactions }
                                premiumFeatureRow(i18nContext.get(DashboardI18nKeysData.PremiumKeys.ServerPremium.Features.TaxFreeFridays), tablePlans, { it.highlight }) { it.plan.taxFreeFridays }
                                premiumFeatureRow(i18nContext.get(DashboardI18nKeysData.PremiumKeys.ServerPremium.Features.TaxFreeSaturdays), tablePlans, { it.highlight }) { it.plan.taxFreeSaturdays }
                            }
                        }
                    }
                }
            )
        }
    }
}
