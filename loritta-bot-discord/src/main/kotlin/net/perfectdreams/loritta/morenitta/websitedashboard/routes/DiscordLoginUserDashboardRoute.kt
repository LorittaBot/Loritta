package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.content.TextContent
import io.ktor.http.formUrlEncode
import io.ktor.http.userAgent
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondRedirect
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.tables.CachedDiscordUserIdentifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserWebsiteSessions
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.Base58
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer.Companion.WEBSITE_SESSION_COOKIE
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.discord.DiscordOAuth2Authorization
import net.perfectdreams.loritta.morenitta.websitedashboard.discord.DiscordOAuth2UserIdentification
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert
import java.time.OffsetDateTime
import java.time.ZoneOffset

class DiscordLoginUserDashboardRoute(val website: LorittaDashboardWebServer) : BaseRoute("/discord/login") {
    override suspend fun onRequest(call: ApplicationCall) {
        val accessCode = call.request.queryParameters["code"]
        val guildId = call.request.queryParameters["guild_id"]

        if (accessCode == null)
            error("Missing auth code!")

        // Attempt to authorize the user!

        // TODO: Check if auth failed, show a error message to the user
        val parameters = Parameters.build {
            append("client_id", website.loritta.config.loritta.discord.applicationId.toString())
            append("client_secret", website.loritta.config.loritta.discord.clientSecret)
            append("grant_type", "authorization_code")
            append("code", accessCode)
            append("redirect_uri", "${website.loritta.config.loritta.dashboard.url}/discord/login")
        }

        val resultAsText = website.loritta.http.post {
            url(website.oauth2Endpoints.OAuth2TokenEndpoint)
            userAgent(UserSession.USER_AGENT)

            setBody(TextContent(parameters.formUrlEncode(), ContentType.Application.FormUrlEncoded))
        }.bodyAsText()

        println("Auth Result: $resultAsText")

        val result = Json.decodeFromString<DiscordOAuth2Authorization>(resultAsText)

        // We also want to get the user's information and associate it with the session
        // This way, we can avoid doing round trips every time when sending requests "on behalf" of the user
        // Because we already have the user's IDs!
        val userIdentificationAsText = website.loritta.http.get {
            url(website.oauth2Endpoints.UserIdentificationEndpoint)
            userAgent(UserSession.USER_AGENT)

            header("Authorization", "Bearer ${result.accessToken}")
        }.bodyAsText()

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

            CachedDiscordUserIdentifications.upsert(
                CachedDiscordUserIdentifications.id,
                onUpdateExclude = listOf(CachedDiscordUserIdentifications.createdAt)
            ) {
                it[CachedDiscordUserIdentifications.createdAt] = now
                it[CachedDiscordUserIdentifications.updatedAt] = now

                it[CachedDiscordUserIdentifications.id] = userIdentification.id
                it[CachedDiscordUserIdentifications.username] = userIdentification.username
                it[CachedDiscordUserIdentifications.globalName] = userIdentification.globalName
                it[CachedDiscordUserIdentifications.discriminator] = userIdentification.discriminator
                it[CachedDiscordUserIdentifications.avatarId] = userIdentification.avatar
                it[CachedDiscordUserIdentifications.email] = userIdentification.email
                it[CachedDiscordUserIdentifications.mfaEnabled] = userIdentification.mfaEnabled
                it[CachedDiscordUserIdentifications.accentColor] = userIdentification.accentColor
                it[CachedDiscordUserIdentifications.locale] = userIdentification.locale
                it[CachedDiscordUserIdentifications.verified] = userIdentification.verified
                it[CachedDiscordUserIdentifications.email] = userIdentification.email
                it[CachedDiscordUserIdentifications.flags] = userIdentification.flags
                it[CachedDiscordUserIdentifications.premiumType] = userIdentification.premiumType
                it[CachedDiscordUserIdentifications.publicFlags] = userIdentification.publicFlags
            }

            return@transaction newSessionToken
        }

        this.website.setLorittaSessionCookie(
            call.response.cookies,
            newSessionToken,
            LorittaDashboardWebServer.WEBSITE_SESSION_COOKIE_MAX_AGE
        )

        val i18nContext = website.getI18nContextFromCall(call)

        // Successfully authenticated, let's freaking go!!!
        // If we have a Guild ID associated with it, we'll redirect to a new page that checks if Loritta was *actually* added to the server
        if (guildId != null) {
            call.respondRedirect("/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/$guildId/added", false)
        } else {
            call.respondRedirect("/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/", false)
        }
    }
}