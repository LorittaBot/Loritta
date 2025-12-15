package net.perfectdreams.loritta.morenitta.websitedashboard

import io.ktor.server.application.ApplicationCall
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserWebsiteSessions
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.websitedashboard.discord.DiscordOAuth2Authorization
import net.perfectdreams.loritta.morenitta.websitedashboard.discord.DiscordOAuth2UserIdentification
import org.jetbrains.exposed.sql.update
import java.time.OffsetDateTime

class LorittaUserSession(
    val loritta: LorittaBot,
    val dashboardWebServer: LorittaDashboardWebServer,
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
            dashboardWebServer.revokeLorittaSessionCookie(call)
            return null
        }
    }

    override suspend fun updateCachedUserInfoExternally(userIdentification: DiscordOAuth2UserIdentification) {
        loritta.transaction {
            dashboardWebServer.updateCachedDiscordUserIdentification(userIdentification)
        }
    }

    override suspend fun updateWebsiteSessionExternally(generatedAt: OffsetDateTime, authorization: DiscordOAuth2Authorization) {
        loritta.transaction {
            UserWebsiteSessions.update({ UserWebsiteSessions.token eq this@LorittaUserSession.websiteToken }) {
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