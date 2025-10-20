package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.server.application.*
import io.ktor.server.response.respondText
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserWebsiteSettings
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.selectAll

abstract class RequiresUserAuthDashboardLocalizedRoute(website: LorittaDashboardWebServer, originalPath: String) : DashboardLocalizedRoute(website, originalPath) {
    override suspend fun onLocalizedRequest(call: ApplicationCall, i18nContext: I18nContext) {
        val session = website.getSession(call)

        if (session == null) {
            return onUnauthenticatedRequest(call, i18nContext)
        }

        val theme = website.loritta.transaction {
            UserWebsiteSettings.selectAll().where {
                UserWebsiteSettings.id eq session.userId
            }.firstOrNull()?.get(UserWebsiteSettings.dashboardColorThemePreference) ?: ColorTheme.SYNC_WITH_SYSTEM
        }

        return onAuthenticatedRequest(call, i18nContext, session, theme)
    }

    abstract suspend fun onAuthenticatedRequest(
        call: ApplicationCall,
        i18nContext: I18nContext,
        session: UserSession,
        theme: ColorTheme
    )

    suspend fun onUnauthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext) {
        call.respondText("Requires Login!")
    }
}