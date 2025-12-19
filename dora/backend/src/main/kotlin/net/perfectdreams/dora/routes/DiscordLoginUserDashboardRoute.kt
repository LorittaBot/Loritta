package net.perfectdreams.dora.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import net.perfectdreams.dora.DoraBackend
import net.perfectdreams.dora.PermissionLevel
import net.perfectdreams.dora.tables.UserWebsiteSessions
import net.perfectdreams.dora.tables.Users
import net.perfectdreams.dora.utils.Base58
import net.perfectdreams.loritta.morenitta.websitedashboard.DiscordAuthenticationResult
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.OffsetDateTime
import java.time.ZoneOffset

class DiscordLoginUserDashboardRoute(val website: DoraBackend) : BaseRoute("/discord/login") {
    override suspend fun onRequest(call: ApplicationCall) {
        val authenticationResult = website.oauth2Manager.authenticate<Unit>(
            website.config.discord.applicationId,
            website.config.discord.clientSecret,
            website.config.websiteUrl.removeSuffix("/") + "/discord/login",
            listOf("identify", "email"),
            call.request.queryParameters["code"],
            null,
            null,
            null,
            call.request.queryParameters["error"],
            call.request.queryParameters["error_description"]
        )

        when (authenticationResult) {
            is DiscordAuthenticationResult.ClientSideError -> {
                call.respondText("Result: $authenticationResult")
            }
            DiscordAuthenticationResult.DiscordInternalServerError -> {
                call.respondText("Result: $authenticationResult")
            }
            DiscordAuthenticationResult.MissingAccessCode -> {
                call.respondText("Result: $authenticationResult")
            }
            is DiscordAuthenticationResult.MissingScopes -> {
                call.respondText("Result: $authenticationResult")
            }
            is DiscordAuthenticationResult.TokenExchangeError -> {
                call.respondText("Result: $authenticationResult")
            }
            is DiscordAuthenticationResult.TamperedState -> {
                call.respondText("Result: $authenticationResult")
            }
            is DiscordAuthenticationResult.Success<*> -> {
                val result = authenticationResult.authorization
                val userIdentification = authenticationResult.userIdentification

                val now = OffsetDateTime.now(ZoneOffset.UTC)

                // If we already have a session token, try updating the session token data instead of creating a new session
                val sessionToken = call.request.cookies[DoraBackend.WEBSITE_SESSION_COOKIE]

                val newSessionToken = website.pudding.transaction {
                    fun createNewSession(): String {
                        val tokenAsBytes = ByteArray(64)
                        website.random.nextBytes(tokenAsBytes)
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
                            it[UserWebsiteSessions.cookieMaxAge] = DoraBackend.WEBSITE_SESSION_COOKIE_MAX_AGE
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
                                it[UserWebsiteSessions.cookieMaxAge] = DoraBackend.WEBSITE_SESSION_COOKIE_MAX_AGE
                            }

                            sessionToken
                        } else {
                            createNewSession()
                        }
                    } else {
                        createNewSession()
                    }

                    website.updateCachedDiscordUserIdentification(userIdentification)

                    // Add the user to the list if it doesn't exist
                    val doesNotExist = Users.selectAll().where {
                        Users.id eq userIdentification.id
                    }.count() == 0L

                    if (doesNotExist) {
                        Users.insert {
                            it[Users.id] = userIdentification.id
                            it[Users.permissionLevel] = PermissionLevel.USER
                        }
                    }

                    return@transaction newSessionToken
                }

                this.website.setLorittaSessionCookie(
                    call.response.cookies,
                    newSessionToken,
                    DoraBackend.WEBSITE_SESSION_COOKIE_MAX_AGE
                )

                call.respondRedirect("/")
            }
        }
    }
}