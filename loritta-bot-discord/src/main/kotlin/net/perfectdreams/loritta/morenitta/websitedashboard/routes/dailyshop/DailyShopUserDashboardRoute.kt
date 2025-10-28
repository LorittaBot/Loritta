package net.perfectdreams.loritta.morenitta.websitedashboard.routes.dailyshop

import io.ktor.server.application.ApplicationCall
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.p
import kotlinx.html.stream.createHTML
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.website.components.EtherealGambiUtils.etherealGambiImg
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dailyShopItems
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.svgIcon
import net.perfectdreams.loritta.morenitta.websitedashboard.components.userDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme

class DailyShopUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/daily-shop") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val dreamStorageServiceNamespace = website.loritta.dreamStorageService.getCachedNamespaceOrRetrieve()
        // Hacky!
        val locale = website.loritta.localeManager.getLocaleById("default")

        val dailyShop = website.loritta.transaction {
            DashboardDailyShopUtils.queryDailyShopResult(
                website.loritta,
                session.userId,
                dreamStorageServiceNamespace
            )
        }

        // A bit hacky but hey, there's nothing a lot we can do rn
        val galleryOfDreamsResponse = website.loritta.cachedGalleryOfDreamsDataResponse!!

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.DailyShop.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                {
                    userDashLeftSidebarEntries(website.loritta, i18nContext, userPremiumPlan, UserDashboardSection.TRINKETS_SHOP)
                },
                {
                    div(classes = "hero-wrapper") {
                        etherealGambiImg("https://stuff.loritta.website/loritta-daily-shop-allouette.png", classes = "hero-image", sizes = "(max-width: 900px) 100vw, 360px") {}

                        div(classes = "hero-text") {
                            h1 {
                                text(i18nContext.get(DashboardI18nKeysData.DailyShop.Title))
                            }

                            p {
                                +"Bem-vind@ a loja diária de itens! O lugar para comprar itens para o seu \"+perfil\" da Loritta!"
                            }
                            p {
                                +"Todo o dia as 00:00 UTC (21:00 no horário do Brasil) a loja é atualizada com novos itens! Então volte todo o dia para verificar ^-^"
                            }
                        }
                    }

                    div(classes = "shop-reset-timer") {
                        div(classes = "horizontal-line") {}

                        svgIcon(SVGIcons.TimerAnimatedHand) {
                            classNames(setOf("stopwatch"))
                        }

                        div(classes = "shop-timer") {
                            attributes["bliss-sse"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/daily-shop/timer"

                            div(classes = "shop-timer-date") {
                                text(DateUtils.formatDateDiff(i18nContext, System.currentTimeMillis(), DashboardDailyShopUtils.getShopResetsEpochMilli(), maxParts = 1))
                            }

                            div(classes = "shop-timer-subtitle") {
                                text("até a loja atualizar")
                            }
                        }
                    }

                    div {
                        id = "loritta-items-wrapper"

                        dailyShopItems(
                            i18nContext,
                            locale,
                            dailyShop,
                            galleryOfDreamsResponse
                        )
                    }
                }
            )
        }
    }
}