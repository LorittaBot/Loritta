package net.perfectdreams.loritta.morenitta.websitedashboard.routes.userpremiumkeys

import io.ktor.server.application.*
import kotlinx.html.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.UserPremiumPlan
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.UserDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.components.PremiumPlanColumn
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.paymentHeroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.premiumBillingToggle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.premiumFeatureRow
import net.perfectdreams.loritta.morenitta.websitedashboard.components.premiumFeatureTableHead
import net.perfectdreams.loritta.morenitta.websitedashboard.components.premiumPlanCards
import net.perfectdreams.loritta.morenitta.websitedashboard.components.premiumValueRow
import net.perfectdreams.loritta.morenitta.websitedashboard.components.userDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import kotlin.math.floor

class UserPremiumKeysUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/premium") {
    data class PlanColumn(
        val name: String,
        val plan: UserPremiumPlan,
        val planValue: Int,
        val monthlyPriceCents: Long,
        val highlight: Boolean
    )

    private fun formatPercentage(pctValue: Double): String {
        val rounded = Math.round(pctValue * 10) / 10.0
        return if (rounded == floor(rounded)) "${rounded.toInt()}%" else "$rounded%"
    }

    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlan, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val plans = listOf(
            PlanColumn(i18nContext.get(DashboardI18nKeysData.PremiumKeys.PlanNames.Basic), UserPremiumPlan.Basic, 25, 2499L, false),
            PlanColumn(i18nContext.get(DashboardI18nKeysData.PremiumKeys.PlanNames.Complete), UserPremiumPlan.Complete, 35, 3499L, true)
        )

        val tablePlans = listOf(
            PlanColumn(i18nContext.get(DashboardI18nKeysData.PremiumKeys.PlanNames.Free), UserPremiumPlan.Free, 0, 0L, false),
            PlanColumn(i18nContext.get(DashboardI18nKeysData.PremiumKeys.PlanNames.Basic), UserPremiumPlan.Basic, 25, 2499L, false),
            PlanColumn(i18nContext.get(DashboardI18nKeysData.PremiumKeys.PlanNames.Complete), UserPremiumPlan.Complete, 35, 3499L, true)
        )

        val localePath = i18nContext.get(net.perfectdreams.loritta.i18n.I18nKeysData.Website.LocalePathId)
        val pageTitle = i18nContext.get(DashboardI18nKeysData.PremiumKeys.UserPremium.PageTitle)

        call.respondHtml {
            dashboardBase(
                i18nContext,
                pageTitle,
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, false),
                {
                    userDashLeftSidebarEntries(website.loritta, i18nContext, userPremiumPlan, UserDashboardSection.USER_PREMIUM_KEYS)
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

                    if (userPremiumPlan != UserPremiumPlan.Free) {
                        div {
                            style = "background: var(--loritta-blue); color: white; padding: 1em; border-radius: 8px; margin-bottom: 1em; text-align: center;"

                            p {
                                text(i18nContext.get(DashboardI18nKeysData.PremiumKeys.UserPremium.CurrentPlan))
                                b {
                                    text(
                                        when (userPremiumPlan) {
                                            UserPremiumPlan.Basic -> i18nContext.get(DashboardI18nKeysData.PremiumKeys.PlanNames.Basic)
                                            UserPremiumPlan.Complete -> i18nContext.get(DashboardI18nKeysData.PremiumKeys.PlanNames.Complete)
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
                            plans.map { PremiumPlanColumn(it.name, it.planValue.toDouble(), it.monthlyPriceCents, it.highlight) },
                            "/$localePath/premium/buy"
                        )
                    }

                    div {
                        style = "overflow-x: auto;"

                        table {
                            style = "width: 100%; border-collapse: collapse; text-align: center;"

                            premiumFeatureTableHead(
                                i18nContext,
                                tablePlans.map { PremiumPlanColumn(it.name, it.planValue.toDouble(), it.monthlyPriceCents, it.highlight) }
                            )

                            val noTax = i18nContext.get(DashboardI18nKeysData.PremiumKeys.UserPremium.Values.NoTax)

                            tbody {
                                premiumFeatureRow(i18nContext.get(DashboardI18nKeysData.PremiumKeys.UserPremium.Features.NoAdsOnDashboard), tablePlans, { it.highlight }) { it.plan.displayAds == false }
                                premiumFeatureRow(i18nContext.get(DashboardI18nKeysData.PremiumKeys.UserPremium.Features.CustomProfileBackground), tablePlans, { it.highlight }) { it.plan.customBackground }
                                premiumFeatureRow(i18nContext.get(DashboardI18nKeysData.PremiumKeys.UserPremium.Features.CustomEmojisInAboutMe), tablePlans, { it.highlight }) { it.plan.customEmojisInAboutMe }
                                premiumFeatureRow(i18nContext.get(DashboardI18nKeysData.PremiumKeys.UserPremium.Features.CustomEmojisInEmojiFight), tablePlans, { it.highlight }) { it.plan.customEmojisInEmojiFight }
                                premiumFeatureRow(i18nContext.get(DashboardI18nKeysData.PremiumKeys.UserPremium.Features.NoDailyInactivityTax), tablePlans, { it.highlight }) { !it.plan.hasDailyInactivityTax }

                                premiumValueRow(i18nContext.get(DashboardI18nKeysData.PremiumKeys.UserPremium.Features.MaxDailyDreams), tablePlans, { it.highlight }) { "${it.plan.maxDreamsInDaily}" }
                                premiumValueRow(i18nContext.get(DashboardI18nKeysData.PremiumKeys.UserPremium.Features.LoriReputationRetribution), tablePlans, { it.highlight }) { formatPercentage(it.plan.loriReputationRetribution) }
                                premiumValueRow(i18nContext.get(DashboardI18nKeysData.PremiumKeys.UserPremium.Features.CoinFlipTax), tablePlans, { it.highlight }) {
                                    if (it.plan.coinFlipRewardTax == 0.0) noTax else formatPercentage(it.plan.coinFlipRewardTax * 100)
                                }
                                premiumValueRow(i18nContext.get(DashboardI18nKeysData.PremiumKeys.UserPremium.Features.ThirdPartyTransferTax), tablePlans, { it.highlight }) {
                                    if (it.plan.thirdPartySonhosTransferTax == 0.0) noTax else formatPercentage(it.plan.thirdPartySonhosTransferTax * 100)
                                }
                                premiumValueRow(i18nContext.get(DashboardI18nKeysData.PremiumKeys.UserPremium.Features.LoraffaTax), tablePlans, { it.highlight }) {
                                    val tax = 1.0 - it.plan.totalLoraffleReward
                                    if (tax == 0.0) noTax else formatPercentage(tax * 100)
                                }
                                premiumValueRow(i18nContext.get(DashboardI18nKeysData.PremiumKeys.UserPremium.Features.LotterittaTax), tablePlans, { it.highlight }) {
                                    val tax = 1.0 - it.plan.totalLotteryReward
                                    if (tax == 0.0) noTax else formatPercentage(tax * 100)
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}
