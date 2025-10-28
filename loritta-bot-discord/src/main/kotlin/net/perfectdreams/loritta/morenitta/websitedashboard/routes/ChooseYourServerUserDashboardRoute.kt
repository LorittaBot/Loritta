package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.server.application.*
import kotlinx.html.div
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.stream.createHTML
import kotlinx.html.textInput
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.loadingSpinnerImage
import net.perfectdreams.loritta.morenitta.websitedashboard.components.userDashLeftSidebarEntries
import net.perfectdreams.loritta.serializable.ColorTheme

class ChooseYourServerUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        call.respondHtml(
            createHTML()
                .html {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.ChooseAServer.Title),
                        session,
                        theme,
                        shimejiSettings,
                        userPremiumPlan,
                        {
                            userDashLeftSidebarEntries(website.loritta, i18nContext, UserDashboardSection.CHOOSE_YOUR_SERVER)
                        },
                        {
                            div {
                                id = "user-guilds"
                                attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds"
                                attributes["bliss-trigger"] = "load"
                                attributes["bliss-swap:200"] = "body (innerHTML) -> this (innerHTML)"
                                attributes["bliss-indicator"] = "this"

                                div(classes = "fill-loading-screen") {
                                    loadingSpinnerImage()

                                    text("Carregando Servidores...")
                                }
                            }
                        }
                    )
                }
        )
    }
}