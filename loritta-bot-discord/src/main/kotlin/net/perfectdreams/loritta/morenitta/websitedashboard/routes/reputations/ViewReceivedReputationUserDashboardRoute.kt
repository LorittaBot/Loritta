package net.perfectdreams.loritta.morenitta.websitedashboard.routes.reputations

import io.ktor.server.application.*
import io.ktor.server.util.getOrFail
import kotlinx.html.hr
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reputations
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.goBackToPreviousSectionButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.reputationInfo
import net.perfectdreams.loritta.morenitta.websitedashboard.components.userDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll

class ViewReceivedReputationUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/reputations/received/{reputationId}") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val reputationId = call.parameters.getOrFail("reputationId").toLong()
        val page = call.request.queryParameters["page"]?.toInt() ?: 1

        val userProfile = website.loritta.getLorittaProfile(session.userId)
        val reputation = website.loritta.transaction {
            Reputations.selectAll()
                .where {
                    (Reputations.id eq reputationId) and (Reputations.givenById eq session.userId or (Reputations.receivedById eq session.userId))
                }
                .first()
        }

        val userInfo = website.loritta.lorittaShards.retrieveUserInfoById(reputation[Reputations.givenById])

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.Reputations.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    userDashLeftSidebarEntries(website.loritta, i18nContext, userPremiumPlan, UserDashboardSection.REPUTATIONS)
                },
                {
                    goBackToPreviousSectionButton(href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/reputations/received?page=$page") {
                        text("Voltar para a lista de reputações")
                    }

                    hr {}

                    reputationInfo(
                        i18nContext,
                        userInfo,
                        reputation,
                        true,
                        page,
                        userProfile?.money ?: 0L
                    )
                }
            )
        }
    }
}