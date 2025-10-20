package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.header
import io.ktor.server.response.respond
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.stream.createHTML
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserWebsiteSessions
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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

class PostLogoutUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/logout") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme) {
        website.loritta.transaction {
            UserWebsiteSessions.deleteWhere {
                UserWebsiteSessions.token eq session.websiteToken
            }
        }

        call.response.cookies.append(
            Cookie(
                LorittaDashboardWebServer.WEBSITE_SESSION_COOKIE,
                "",
                path = "/", // Available in any path of the domain
                // secure = true, // Only sent via HTTPS
                httpOnly = true, // Disable JS access
                maxAge = 0
            )
        )
        call.response.header("Bliss-Redirect", website.loritta.config.loritta.website.url)
        call.respond(HttpStatusCode.NoContent)
    }
}