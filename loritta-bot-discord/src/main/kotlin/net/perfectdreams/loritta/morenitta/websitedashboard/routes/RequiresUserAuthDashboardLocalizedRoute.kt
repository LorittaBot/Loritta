package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.origin
import io.ktor.server.request.host
import io.ktor.server.request.uri
import io.ktor.server.request.userAgent
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.meta
import kotlinx.html.p
import kotlinx.html.title
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserPocketLorittaSettings
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserWebsiteSettings
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.LorittaDiscordOAuth2AuthorizeScopeURL
import net.perfectdreams.loritta.morenitta.website.utils.extensions.hostFromHeader
import net.perfectdreams.loritta.morenitta.websitedashboard.AuthenticationState
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.AuthenticationStateUtils
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
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

    open suspend fun onUnauthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext) {
        if (call.request.userAgent() == Constants.DISCORD_CRAWLER_USER_AGENT) {
            respondWithDiscordLoginPage(
                call,
                i18nContext.get(I18nKeysData.Website.Dashboard.TitleShort),
                "Meu painel de configuração, aonde você pode me configurar para deixar o seu servidor único e incrível!",
                "https://stuff.loritta.website/loritta-and-wumpus-dashboard-yafyr.png"
            )
            return
        }

        respondWithDiscordAuthRedirect(call)
    }

    suspend fun respondWithDiscordLoginPage(
        call: ApplicationCall,
        embedTitle: String,
        embedDescription: String,
        embedImageUrl: String
    ) {
        call.respondHtml {
            head {
                fun setMetaProperty(property: String, content: String) {
                    meta(content = content) { attributes["property"] = property }
                }

                title("Login • Loritta")
                setMetaProperty("og:site_name", "Loritta")
                setMetaProperty("og:title", embedTitle)
                setMetaProperty("og:description", embedDescription)
                setMetaProperty("og:image", embedImageUrl)
                setMetaProperty("og:image:width", "320")
                setMetaProperty("og:ttl", "660")
                setMetaProperty("og:image:width", "320")
                setMetaProperty("theme-color", LorittaColors.LorittaAqua.toHex())
                meta("twitter:card", "summary_large_image")
            }
            body {
                p {
                    + "Parabéns, você encontrou um easter egg!"
                }
            }
        }
    }

    suspend fun respondWithDiscordAuthRedirect(call: ApplicationCall) {
        val fullUrl = call.request.origin.scheme + "://" + call.request.hostFromHeader() + call.request.uri

        call.respondRedirect(
            LorittaDiscordOAuth2AuthorizeScopeURL(
                website.loritta,
                AuthenticationStateUtils.createStateAsBase64(
                    AuthenticationState(
                        redirectUrl = fullUrl,
                    ),
                    website.loritta
                )
            ),
            false
        )
    }
}