package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.server.application.*
import io.ktor.server.response.respondText
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserPocketLorittaSettings
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserWebsiteSettings
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.ActivityLevel
import org.jetbrains.exposed.sql.selectAll

abstract class RequiresUserAuthDashboardLocalizedRoute(website: LorittaDashboardWebServer, originalPath: String) : DashboardLocalizedRoute(website, originalPath) {
    override suspend fun onLocalizedRequest(call: ApplicationCall, i18nContext: I18nContext) {
        val session = website.getSession(call)

        if (session == null) {
            return onUnauthenticatedRequest(call, i18nContext)
        }

        val (userPremiumPlan, theme, settings) = website.loritta.transaction {
            val userPremiumPlan = UserPremiumPlans.getPlanFromValue(website.loritta._getActiveMoneyFromDonations(session.userId))

            val theme = UserWebsiteSettings.selectAll().where {
                UserWebsiteSettings.id eq session.userId
            }.firstOrNull()?.get(UserWebsiteSettings.dashboardColorThemePreference) ?: ColorTheme.SYNC_WITH_SYSTEM

            val settings = UserPocketLorittaSettings.selectAll()
                .where {
                    UserPocketLorittaSettings.id eq session.userId
                }
                .firstOrNull()
                .let {
                    LorittaShimejiSettings(
                        it?.get(UserPocketLorittaSettings.lorittaCount) ?: 0,
                        it?.get(UserPocketLorittaSettings.pantufaCount) ?: 0,
                        it?.get(UserPocketLorittaSettings.gabrielaCount) ?: 0,
                        it?.get(UserPocketLorittaSettings.activityLevel) ?: ActivityLevel.MEDIUM
                    )
                }

            Triple(userPremiumPlan, theme, settings)
        }

        return onAuthenticatedRequest(call, i18nContext, session, userPremiumPlan, theme, settings)
    }

    abstract suspend fun onAuthenticatedRequest(
        call: ApplicationCall,
        i18nContext: I18nContext,
        session: UserSession,
        userPremiumPlan: UserPremiumPlans,
        theme: ColorTheme,
        shimejiSettings: LorittaShimejiSettings
    )

    suspend fun onUnauthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext) {
        call.respondText("Requires Login!")
    }
}