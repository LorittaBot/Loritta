package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.server.application.*
import kotlinx.html.a
import kotlinx.html.b
import kotlinx.html.br
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.hr
import kotlinx.html.id
import kotlinx.html.style
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.UserDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.components.cardHeader
import net.perfectdreams.loritta.morenitta.websitedashboard.components.cardHeaderDescription
import net.perfectdreams.loritta.morenitta.websitedashboard.components.cardHeaderInfo
import net.perfectdreams.loritta.morenitta.websitedashboard.components.cardHeaderTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.cardsWithHeader
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.loadingSpinnerImage
import net.perfectdreams.loritta.morenitta.websitedashboard.components.userDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme

class ChooseYourServerUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.ChooseAServer.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    userDashLeftSidebarEntries(website.loritta, i18nContext, userPremiumPlan, UserDashboardSection.CHOOSE_YOUR_SERVER)
                },
                {
                    div {
                        h1 {
                            style = "text-align: center;"

                            text(i18nContext.get(DashboardI18nKeysData.ChooseAServer.Title))
                        }

                        hr {}
                    }

                    div {
                        id = "user-guilds"
                        attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds"
                        attributes["bliss-trigger"] = "load"
                        attributes["bliss-swap:200"] = "body (innerHTML) -> this (innerHTML)"
                        attributes["bliss-indicator"] = "this"

                        div(classes = "fill-loading-screen") {
                            loadingSpinnerImage()

                            text(i18nContext.get(DashboardI18nKeysData.LoadingServers))
                        }
                    }
                }
            )
        }
    }
}