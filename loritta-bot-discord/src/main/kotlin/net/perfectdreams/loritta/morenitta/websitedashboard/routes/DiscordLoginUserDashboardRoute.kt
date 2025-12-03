package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.html.title
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.cinnamon.pudding.tables.LorittaAddedGuildsStats
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserWebsiteSessions
import net.perfectdreams.loritta.common.utils.LORITTA_AUTHORIZATION_SCOPES
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.Base58
import net.perfectdreams.loritta.morenitta.utils.LorittaDiscordOAuth2AddBotURL
import net.perfectdreams.loritta.morenitta.utils.LorittaDiscordOAuth2AuthorizeScopeURL
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer.Companion.WEBSITE_SESSION_COOKIE
import net.perfectdreams.loritta.morenitta.websitedashboard.AuthenticationState
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.authorizationFailedFullScreenError
import net.perfectdreams.loritta.morenitta.websitedashboard.components.websiteBase
import net.perfectdreams.loritta.morenitta.websitedashboard.discord.DiscordOAuth2Authorization
import net.perfectdreams.loritta.morenitta.websitedashboard.discord.DiscordOAuth2UserIdentification
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.AuthenticationStateUtils
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.dashboardTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Base64

class DiscordLoginUserDashboardRoute(val website: LorittaDashboardWebServer) : BaseRoute("/discord/login") {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

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

        val state = if (stateAsString != null) {
            try {
                val decodedData = AuthenticationStateUtils.verifyAndExtract(
                    Base64.getUrlDecoder().decode(stateAsString).toString(Charsets.UTF_8),
                    website.loritta.config.loritta.dashboard.authenticationStateKey
                )

                if (decodedData == null) {
                    respondUserFriendlyAuthenticationFailed(
                        call,
                        i18nContext.get(I18nKeysData.Website.Dashboard.AuthorizationFailedFullScreenError.Errors.TamperedState),
                        true
                    )
                    return
                }

                Json.decodeFromString<AuthenticationState>(decodedData)
            } catch (e: Exception) {
                logger.info(e) { "User authentication failed! State failed to be validated and decoded. State was $stateAsString" }
                respondUserFriendlyAuthenticationFailed(
                    call,
                    i18nContext.get(I18nKeysData.Website.Dashboard.AuthorizationFailedFullScreenError.Errors.TamperedState),
                    true
                )
                return
            }
        } else null

        if (error != null) {
            // oof, something went wrong!
            val errorDescription = call.request.queryParameters["error_description"]

            logger.info { "User authentication failed! Error was $error, error description was $errorDescription" }
            respondUserFriendlyAuthenticationFailed(
                call,
                if (error == "access_denied" && errorDescription == "The resource owner or authorization server denied the request") {
                    i18nContext.get(I18nKeysData.Website.Dashboard.AuthorizationFailedFullScreenError.Errors.ResourceOwnerOrAuthorizationServerDeniedTheRequest)
                } else errorDescription ?: error,
                false
            )
            return
        }

        if (accessCode == null) {
            respondUserFriendlyAuthenticationFailed(
                call,
                i18nContext.get(I18nKeysData.Website.Dashboard.AuthorizationFailedFullScreenError.Errors.MissingAuthenticationCode),
                false
            )
            return
        }

        // Attempt to authorize the user!
        val parameters = Parameters.build {
            append("client_id", website.loritta.config.loritta.discord.applicationId.toString())
            append("client_secret", website.loritta.config.loritta.discord.clientSecret)
            append("grant_type", "authorization_code")
            append("code", accessCode)
            append("redirect_uri", "${website.loritta.config.loritta.dashboard.url}/discord/login")
        }

        val oauth2TokenHttpResponse = website.loritta.http.post {
            url(website.oauth2Endpoints.OAuth2TokenEndpoint)
            userAgent(UserSession.USER_AGENT)

            setBody(TextContent(parameters.formUrlEncode(), ContentType.Application.FormUrlEncoded))
        }

        logger.info { "Authentication Result (Status Code: ${oauth2TokenHttpResponse.status}): $oauth2TokenHttpResponse" }

        if (oauth2TokenHttpResponse.status == HttpStatusCode.InternalServerError) {
            logger.info { "User authentication failed! Discord sent a internal server error during OAuth2 token request" }

            respondUserFriendlyAuthenticationFailed(
                call,
                i18nContext.get(I18nKeysData.Website.Dashboard.AuthorizationFailedFullScreenError.Errors.DiscordInternalServerError),
                false
            )
            return
        }

        val oauth2TokenResultAsText = oauth2TokenHttpResponse.bodyAsText()
        val resultAsJson = Json.parseToJsonElement(oauth2TokenResultAsText).jsonObject

        if (resultAsJson.containsKey("error")) {
            val error = resultAsJson["error"]!!.jsonPrimitive.content
            val errorDescription = resultAsJson["error_description"]?.jsonPrimitive?.content
            logger.info { "User authentication failed! Error was $error, error description was $errorDescription" }

            respondUserFriendlyAuthenticationFailed(
                call,
                if (error == "invalid_grant" && errorDescription == "Invalid \"code\" in request.") {
                    i18nContext.get(I18nKeysData.Website.Dashboard.AuthorizationFailedFullScreenError.Errors.InvalidAuthenticationCode)
                } else errorDescription ?: error,
                false
            )
            return
        }

        val result = Json.decodeFromJsonElement<DiscordOAuth2Authorization>(resultAsJson)

        // When testing this: Remember that Discord "persists" your previously authorized scopes
        val authorizedScopes = result.scope.split(" ")
        val hasAllRequiredScopes = LORITTA_AUTHORIZATION_SCOPES.all { it in authorizedScopes }
        if (!hasAllRequiredScopes) {
            logger.info { "User authentication failed! We need $LORITTA_AUTHORIZATION_SCOPES but user only authorized $authorizedScopes" }

            respondUserFriendlyAuthenticationFailed(
                call,
                i18nContext.get(I18nKeysData.Website.Dashboard.AuthorizationFailedFullScreenError.Errors.MissingScopes),
                false
            )
            return
        }

        // We also want to get the user's information and associate it with the session
        // This way, we can avoid doing round trips every time when sending requests "on behalf" of the user
        // Because we already have the user's IDs!
        val userIdentificationHttpResponse = website.loritta.http.get {
            url(website.oauth2Endpoints.UserIdentificationEndpoint)
            userAgent(UserSession.USER_AGENT)

            header("Authorization", "Bearer ${result.accessToken}")
        }

        val userIdentificationAsText = userIdentificationHttpResponse.bodyAsText()
        logger.info { "User Identification Result (${userIdentificationHttpResponse.status}): $userIdentificationAsText" }

        if (oauth2TokenHttpResponse.status == HttpStatusCode.InternalServerError) {
            logger.info { "User authentication failed! Discord sent a internal server error during user identification request" }

            respondUserFriendlyAuthenticationFailed(
                call,
                i18nContext.get(I18nKeysData.Website.Dashboard.AuthorizationFailedFullScreenError.Errors.DiscordInternalServerError),
                false
            )
            return
        }

        val userIdentification = Json.decodeFromString<DiscordOAuth2UserIdentification>(userIdentificationAsText)

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