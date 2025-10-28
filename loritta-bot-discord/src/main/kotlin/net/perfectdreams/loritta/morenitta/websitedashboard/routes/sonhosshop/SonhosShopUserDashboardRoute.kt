package net.perfectdreams.loritta.morenitta.websitedashboard.routes.sonhosshop

import io.ktor.server.application.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosBundles
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fancyDetails
import net.perfectdreams.loritta.morenitta.websitedashboard.components.paymentHeroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.sonhosBundlesWithCouponInput
import net.perfectdreams.loritta.morenitta.websitedashboard.components.userDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.SonhosBundle
import org.jetbrains.exposed.sql.selectAll

class SonhosShopUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/sonhos-shop") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val sonhosBundles = website.loritta.transaction {
            SonhosBundles.selectAll()
                .where { SonhosBundles.active eq true }
                .toList()
        }.map {
            SonhosBundle(
                it[SonhosBundles.id].value,
                it[SonhosBundles.active],
                it[SonhosBundles.price],
                it[SonhosBundles.sonhos],
                it[SonhosBundles.bonus]
            )
        }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.SonhosShop.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                // This may seem stupid, but hear me out:
                // We don't want to distract users with ads when buying sonhos!
                // So we hide all ads on the sonhos shop page :3
                false,
                {
                    userDashLeftSidebarEntries(website.loritta, i18nContext, userPremiumPlan, UserDashboardSection.SONHOS_SHOP)
                },
                {
                    div {
                        style = "text-align: center;"

                        paymentHeroWrapper(i18nContext)

                        hr {}

                        h1 {
                            text(i18nContext.get(DashboardI18nKeysData.SonhosShop.Title))
                        }
                    }

                    div {
                        id = "sonhos-bundles-with-coupon-wrapper"
                        style = "gap: 1em; display: flex; flex-direction: column; justify-content: left; gap: 1em;"

                        sonhosBundlesWithCouponInput(i18nContext, sonhosBundles, null)
                    }

                    hr {}

                    h2 { text(i18nContext.get(DashboardI18nKeysData.FrequentlyAskedQuestions)) }

                    div(classes = "fancy-details-wrapper") {
                        fancyDetails(
                            {
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.SonhosShop.Faq.WhyCanIBuySonhos.Title))
                            },
                            {
                                for (line in i18nContext.get(I18nKeysData.Website.Dashboard.SonhosShop.Faq.WhyCanIBuySonhos.Description)) {
                                    p {
                                        text(line)
                                    }
                                }
                            }
                        )

                        fancyDetails(
                            {
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.SonhosShop.Faq.HowMuchTimeItTakesToReceiveTheSonhos.Title))
                            },
                            {
                                for (line in i18nContext.get(I18nKeysData.Website.Dashboard.SonhosShop.Faq.HowMuchTimeItTakesToReceiveTheSonhos.Description)) {
                                    p {
                                        text(line)
                                    }
                                }
                            }
                        )

                        fancyDetails(
                            {
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.SonhosShop.Faq.WhyNotBuyWithThirdParties.Title))
                            },
                            {
                                for (line in i18nContext.get(I18nKeysData.Website.Dashboard.SonhosShop.Faq.WhyNotBuyWithThirdParties.Description)) {
                                    p {
                                        text(line)
                                    }
                                }
                            }
                        )

                        fancyDetails(
                            {
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.SonhosShop.Faq.CanIUseMyParentsCard.Title))
                            },
                            {
                                for (line in i18nContext.get(I18nKeysData.Website.Dashboard.SonhosShop.Faq.CanIUseMyParentsCard.Description)) {
                                    p {
                                        text(line)
                                    }
                                }
                            }
                        )

                        fancyDetails(
                            {
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.SonhosShop.Faq.CanIGetARefund.Title))
                            },
                            {
                                for (line in i18nContext.get(I18nKeysData.Website.Dashboard.SonhosShop.Faq.CanIGetARefund.Description)) {
                                    p {
                                        text(line)
                                    }
                                }
                            }
                        )
                    }
                }
            )
        }
    }
}