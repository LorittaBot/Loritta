package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.cinnamon.pudding.tables.LorittaAddedGuildsStats
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserWebsiteSessions
import net.perfectdreams.loritta.common.utils.LORITTA_AUTHORIZATION_SCOPES
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.Base58
import net.perfectdreams.loritta.morenitta.utils.LorittaDiscordOAuth2AddBotURL
import net.perfectdreams.loritta.morenitta.utils.LorittaDiscordOAuth2AuthorizeScopeURL
import net.perfectdreams.loritta.morenitta.websitedashboard.AuthenticationState
import net.perfectdreams.loritta.morenitta.websitedashboard.DiscordAuthenticationResult
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer.Companion.WEBSITE_SESSION_COOKIE
import net.perfectdreams.loritta.morenitta.websitedashboard.components.authorizationFailedFullScreenError
import net.perfectdreams.loritta.morenitta.websitedashboard.components.websiteBase
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.dashboardTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import java.time.OffsetDateTime
import java.time.ZoneOffset

class DiscordLoginUserDashboardRoute(val website: LorittaDashboardWebServer) : BaseRoute("/discord/login") {
    override suspend fun onRequest(call: ApplicationCall) {
        val accessCode = call.request.queryParameters["code"]
        val guildId = call.request.queryParameters["guild_id"]?.toLongOrNull()
        val stateAsString = call.request.queryParameters["state"]
        val error = call.request.queryParameters["error"]
        val i18nContext = website.getI18nContextFromCall(call)

        suspend fun respondUserFriendlyAuthenticationFailed(
            call: ApplicationCall,
            message: String,
            resetState: Boolean
        ) {
            call.respondHtml(status = HttpStatusCode.Unauthorized) {
                websiteBase(
                    i18nContext,
                    dashboardTitle(i18nContext, i18nContext.get(I18nKeysData.Website.Dashboard.AuthorizationFailedFullScreenError.Title)),
                    // Nasty!
                    // But honestly? Do we even care about doing a bit of static abuse?
                    Url(LorittaDashboardWebServer.INSTANCE.loritta.config.loritta.dashboard.url).host
                ) {
                    authorizationFailedFullScreenError(
                        i18nContext,
                        message,
                        if (guildId != null) {
                            LorittaDiscordOAuth2AddBotURL(
                                website.loritta,
                                guildId,
                                if (!resetState && stateAsString != null)
                                    stateAsString
                                else
                                    null
                            ).toString()
                        } else {
                            LorittaDiscordOAuth2AuthorizeScopeURL(
                                website.loritta,
                                if (!resetState && stateAsString != null)
                                    stateAsString
                                else
                                    null
                            ).toString()
                        }
                    )
                }
            }
        }

        val authenticationResult = website.oauth2Manager.authenticate(
            website.loritta.config.loritta.discord.applicationId,
            website.loritta.config.loritta.discord.clientSecret,
            "${website.loritta.config.loritta.dashboard.url}/discord/login",
            LORITTA_AUTHORIZATION_SCOPES,
            accessCode,
            stateAsString,
            AuthenticationState.serializer(),
            website.loritta.config.loritta.dashboard.authenticationStateKey,
            error,
            call.request.queryParameters["error_description"]
        )

        when (authenticationResult) {
            is DiscordAuthenticationResult.ClientSideError -> {
                respondUserFriendlyAuthenticationFailed(
                    call,
                    if (authenticationResult.error == "access_denied" && authenticationResult.errorDescription == "The resource owner or authorization server denied the request") {
                        i18nContext.get(I18nKeysData.Website.Dashboard.AuthorizationFailedFullScreenError.Errors.ResourceOwnerOrAuthorizationServerDeniedTheRequest)
                    } else authenticationResult.errorDescription ?: authenticationResult.error,
                    false
                )
                return
            }
            DiscordAuthenticationResult.DiscordInternalServerError -> {
                respondUserFriendlyAuthenticationFailed(
                    call,
                    i18nContext.get(I18nKeysData.Website.Dashboard.AuthorizationFailedFullScreenError.Errors.DiscordInternalServerError),
                    false
                )
            }
            DiscordAuthenticationResult.MissingAccessCode -> {
                respondUserFriendlyAuthenticationFailed(
                    call,
                    i18nContext.get(I18nKeysData.Website.Dashboard.AuthorizationFailedFullScreenError.Errors.MissingAuthenticationCode),
                    false
                )
            }
            is DiscordAuthenticationResult.MissingScopes -> {
                respondUserFriendlyAuthenticationFailed(
                    call,
                    i18nContext.get(I18nKeysData.Website.Dashboard.AuthorizationFailedFullScreenError.Errors.MissingScopes),
                    false
                )
            }
            is DiscordAuthenticationResult.TokenExchangeError -> {
                respondUserFriendlyAuthenticationFailed(
                    call,
                    if (authenticationResult.error == "invalid_grant" && authenticationResult.errorDescription == "Invalid \"code\" in request.") {
                        i18nContext.get(I18nKeysData.Website.Dashboard.AuthorizationFailedFullScreenError.Errors.InvalidAuthenticationCode)
                    } else authenticationResult.errorDescription ?: authenticationResult.error,
                    false
                )
            }
            is DiscordAuthenticationResult.TamperedState -> {
                respondUserFriendlyAuthenticationFailed(
                    call,
                    i18nContext.get(I18nKeysData.Website.Dashboard.AuthorizationFailedFullScreenError.Errors.TamperedState),
                    true
                )
            }
            is DiscordAuthenticationResult.Success<*> -> {
                val result = authenticationResult.authorization
                val userIdentification = authenticationResult.userIdentification
                val state = authenticationResult.state as AuthenticationState?

                val now = OffsetDateTime.now(ZoneOffset.UTC)

                // If we already have a session token, try updating the session token data instead of creating a new session
                val sessionToken = call.request.cookies[WEBSITE_SESSION_COOKIE]

                val newSessionToken = website.loritta.transaction {
                    fun createNewSession(): String {
                        val tokenAsBytes = ByteArray(64)
                        website.loritta.random.nextBytes(tokenAsBytes)
                        val token = Base58.encode(tokenAsBytes)

                        UserWebsiteSessions.insert {
                            it[UserWebsiteSessions.token] = token
                            it[UserWebsiteSessions.userId] = userIdentification.id
                            it[UserWebsiteSessions.createdAt] = now
                            it[UserWebsiteSessions.refreshedAt] = now
                            it[UserWebsiteSessions.lastUsedAt] = now
                            it[UserWebsiteSessions.tokenType] = result.tokenType
                            it[UserWebsiteSessions.accessToken] = result.accessToken
                            it[UserWebsiteSessions.expiresIn] = result.expiresIn
                            it[UserWebsiteSessions.refreshToken] = result.refreshToken
                            it[UserWebsiteSessions.scope] = result.scope.split(" ")
                            it[UserWebsiteSessions.cookieSetAt] = now
                            it[UserWebsiteSessions.cookieMaxAge] = LorittaDashboardWebServer.WEBSITE_SESSION_COOKIE_MAX_AGE
                        }

                        return token
                    }

                    if (guildId != null) {
                        LorittaAddedGuildsStats.insert {
                            it[LorittaAddedGuildsStats.guildId] = guildId
                            it[LorittaAddedGuildsStats.addedAt] = now
                            it[LorittaAddedGuildsStats.addedBy] = userIdentification.id
                            it[LorittaAddedGuildsStats.sourceValue] = state?.source
                            it[LorittaAddedGuildsStats.medium] = state?.medium
                            it[LorittaAddedGuildsStats.campaign] = state?.campaign
                            it[LorittaAddedGuildsStats.content] = state?.content
                            it[LorittaAddedGuildsStats.httpReferrer] = state?.httpReferrer
                            it[LorittaAddedGuildsStats.discordLocale] = userIdentification.locale
                        }
                    }

                    val newSessionToken = if (sessionToken != null) {
                        // If the user already has a session token, just attempt to update it with the new data
                        val existingSession = UserWebsiteSessions.select(UserWebsiteSessions.id)
                            .where {
                                UserWebsiteSessions.token eq sessionToken
                            }
                            .firstOrNull()

                        if (existingSession != null) {
                            UserWebsiteSessions.update({ UserWebsiteSessions.token eq sessionToken }) {
                                it[UserWebsiteSessions.userId] = userIdentification.id
                                it[UserWebsiteSessions.createdAt] = now
                                it[UserWebsiteSessions.refreshedAt] = now
                                it[UserWebsiteSessions.lastUsedAt] = now
                                it[UserWebsiteSessions.tokenType] = result.tokenType
                                it[UserWebsiteSessions.accessToken] = result.accessToken
                                it[UserWebsiteSessions.expiresIn] = result.expiresIn
                                it[UserWebsiteSessions.refreshToken] = result.refreshToken
                                it[UserWebsiteSessions.scope] = result.scope.split(" ")
                                it[UserWebsiteSessions.cookieSetAt] = now
                                it[UserWebsiteSessions.cookieMaxAge] = LorittaDashboardWebServer.WEBSITE_SESSION_COOKIE_MAX_AGE
                            }

                            sessionToken
                        } else {
                            createNewSession()
                        }
                    } else {
                        createNewSession()
                    }

                    website.updateCachedDiscordUserIdentification(userIdentification)

                    return@transaction newSessionToken
                }

                this.website.setLorittaSessionCookie(
                    call.response.cookies,
                    newSessionToken,
                    LorittaDashboardWebServer.WEBSITE_SESSION_COOKIE_MAX_AGE
                )

                // Successfully authenticated, let's freaking go!!!
                val redirectUrl = state?.redirectUrl
                if (redirectUrl != null) {
                    // If we have a redirect URL for our state, we will redirect to it
                    call.respondRedirect(redirectUrl, false)
                } else {
                    // If we have a Guild ID associated with it, we'll redirect to a new page that checks if Loritta was *actually* added to the server
                    if (guildId != null) {
                        call.respondRedirect("/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/$guildId/added", false)
                    } else {
                        call.respondRedirect("/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/", false)
                    }
                }
            }
        }
    }
}