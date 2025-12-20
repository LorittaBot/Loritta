package net.perfectdreams.dora.routes

import io.ktor.http.ParametersBuilder
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.server.application.*
import io.ktor.server.response.*
import net.perfectdreams.dora.DoraBackend
import net.perfectdreams.dora.DoraUserSession
import net.perfectdreams.dora.PermissionLevel
import net.perfectdreams.dora.tables.Users
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

abstract class RequiresUserAuthDashboardLocalizedRoute(val website: DoraBackend, originalPath: String) : BaseRoute(originalPath) {
    override suspend fun onRequest(call: ApplicationCall) {
        val session = website.getSession(call)

        if (session == null) {
            return onUnauthenticatedRequest(call)
        }

        val userPermissionLevel = website.pudding.transaction {
            Users.selectAll()
                .where { Users.id eq session.userId }
                .firstOrNull()
                ?.get(Users.permissionLevel)
                ?: PermissionLevel.USER
        }

        return onAuthenticatedRequest(call, session, userPermissionLevel)
    }

    abstract suspend fun onAuthenticatedRequest(
        call: ApplicationCall,
        session: DoraUserSession,
        userPermissionLevel: PermissionLevel
    )

    open suspend fun onUnauthenticatedRequest(call: ApplicationCall) {
        respondWithDiscordAuthRedirect(call)
    }

    suspend fun respondWithDiscordAuthRedirect(call: ApplicationCall) {
        call.respondRedirect(
            DiscordOAuth2AuthorizationURL {
                append("client_id", website.config.discord.applicationId.toString())
                append("response_type", "code")
                append("redirect_uri", website.config.websiteUrl.removeSuffix("/") + "/discord/login")
                append("scope", "identify email")
            },
            false
        )
    }

    /**
     * Builds a Discord OAuth2 authorization URL
     *
     * @params parameters the OAuth2 URL parameters (such as `client_id`, etc)
     */
    fun DiscordOAuth2AuthorizationURL(
        parameters: ParametersBuilder.() -> (Unit)
    ) = URLBuilder(
        protocol = URLProtocol.HTTPS,
        host = "discord.com",
        pathSegments = listOf("oauth2", "authorize"),
        parameters = ParametersBuilder().apply(parameters).build()
    ).build()
}