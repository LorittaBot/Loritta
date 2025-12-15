package net.perfectdreams.loritta.morenitta.websitedashboard.routes.reputations

import io.ktor.server.application.*
import kotlinx.html.hr
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reputations
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.UserDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.components.*
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll

class ReceivedReputationsUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/reputations/received") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val page = (call.request.queryParameters["page"]?.toIntOrNull() ?: 1).coerceAtLeast(1)

        val (reputations, totalReputations) = website.loritta.transaction {
            val totalReputations = Reputations
                .selectAll()
                .where {
                    Reputations.receivedById eq session.userId
                }
                .count()

            val pageReps = Reputations.selectAll()
                .where {
                    Reputations.receivedById eq session.userId
                }
                .orderBy(Reputations.receivedAt, SortOrder.DESC)
                .limit(100)
                .offset((page - 1) * 100L)
                .toList()

            Pair(pageReps, totalReputations)
        }

        val usersInformation = website.loritta.lorittaShards.retrieveUsersInfoById(reputations.map { it[Reputations.receivedById] }.toSet() + reputations.map { it[Reputations.givenById] }.toSet())

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
                    goBackToPreviousSectionButton(href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/reputations") {
                        text("Voltar para a visão geral de reputações")
                    }

                    hr {}

                    reputations(
                        i18nContext,
                        totalReputations,
                        reputations,
                        true,
                        page,
                        usersInformation
                    )
                }
            )
        }
    }
}