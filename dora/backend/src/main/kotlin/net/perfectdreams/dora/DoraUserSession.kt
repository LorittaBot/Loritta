package net.perfectdreams.dora

import io.ktor.server.application.ApplicationCall
import net.perfectdreams.dora.tables.UserWebsiteSessions
import net.perfectdreams.loritta.morenitta.websitedashboard.DiscordOAuth2Manager
import net.perfectdreams.loritta.morenitta.websitedashboard.DiscordUserCredentials
import net.perfectdreams.loritta.morenitta.websitedashboard.UnauthorizedTokenException
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.discord.DiscordOAuth2Authorization
import net.perfectdreams.loritta.morenitta.websitedashboard.discord.DiscordOAuth2UserIdentification
import org.jetbrains.exposed.sql.update
import java.time.OffsetDateTime

class DoraUserSession(
    val dora: DoraBackend,
    oauth2Manager: DiscordOAuth2Manager,
    applicationId: Long,
    clientSecret: String,
    websiteToken: String,
    userId: Long,
    discordUserCredentials: DiscordUserCredentials,
    cachedUserIdentification: UserIdentification
) : UserSession(oauth2Manager, applicationId, clientSecret, websiteToken, userId, discordUserCredentials, cachedUserIdentification) {
    suspend fun retrieveUserIdentificationOrNullIfUnauthorizedRevokeToken(call: ApplicationCall): DiscordOAuth2UserIdentification? {
        try {
            return retrieveUserIdentification()
        } catch (_: UnauthorizedTokenException) {
            dora.revokeLorittaSessionCookie(call)
            return null
        }
    }

    override suspend fun updateCachedUserInfoExternally(userIdentification: DiscordOAuth2UserIdentification) {
        dora.pudding.transaction {
            dora.updateCachedDiscordUserIdentification(userIdentification)
        }
    }

    override suspend fun updateWebsiteSessionExternally(generatedAt: OffsetDateTime, authorization: DiscordOAuth2Authorization) {
        dora.pudding.transaction {
            UserWebsiteSessions.update({ UserWebsiteSessions.token eq this@DoraUserSession.websiteToken }) {
                it[UserWebsiteSessions.refreshedAt] = generatedAt
                it[UserWebsiteSessions.lastUsedAt] = generatedAt
                it[UserWebsiteSessions.tokenType] = authorization.tokenType
                it[UserWebsiteSessions.accessToken] = authorization.accessToken
                it[UserWebsiteSessions.expiresIn] = authorization.expiresIn
                it[UserWebsiteSessions.refreshToken] = authorization.refreshToken
                it[UserWebsiteSessions.scope] = authorization.scope.split(" ")
            }
        }
    }
}