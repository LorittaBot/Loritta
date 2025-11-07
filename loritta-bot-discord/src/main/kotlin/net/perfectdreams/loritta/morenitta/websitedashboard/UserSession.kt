package net.perfectdreams.loritta.morenitta.websitedashboard

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
import kotlinx.serialization.json.Json
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserWebsiteSessions
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.websitedashboard.discord.DiscordOAuth2Authorization
import net.perfectdreams.loritta.morenitta.websitedashboard.discord.DiscordOAuth2Guild
import net.perfectdreams.loritta.morenitta.websitedashboard.discord.DiscordOAuth2UserIdentification
import org.jetbrains.exposed.sql.update
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Base64

class UserSession(
    val loritta: LorittaBot,
    val dashboardWebServer: LorittaDashboardWebServer,
    val websiteToken: String,
    val userId: Long,
    val discordUserCredentials: DiscordUserCredentials,
    var cachedUserIdentification: UserIdentification
) {
    companion object {
        const val USER_AGENT = "Loritta-Morenitta-Discord-Auth/2.0"
        private val logger by HarmonyLoggerFactory.logger {}
    }

    fun getEffectiveAvatarUrl(): String {
        val userAvatarId = this.cachedUserIdentification.avatarId

        val avatarUrl = if (userAvatarId != null) {
            val extension = if (userAvatarId.startsWith("a_")) { // Avatares animados no Discord começam com "_a"
                "gif"
            } else {
                "png"
            }

            "https://cdn.discordapp.com/avatars/${this.userId}/${userAvatarId}.${extension}?size=64"
        } else {
            val avatarId = (this.userId shr 22) % 6

            "https://cdn.discordapp.com/embed/avatars/$avatarId.png"
        }

        return avatarUrl
    }

    suspend fun retrieveUserIdentification(): DiscordOAuth2UserIdentification {
        refreshTokenIfExpired()

        val userIdentificationHttpResponse = loritta.http.get {
            url(dashboardWebServer.oauth2Endpoints.UserIdentificationEndpoint)
            userAgent(USER_AGENT)

            header("Authorization", "Bearer ${this@UserSession.discordUserCredentials.accessToken}")
        }

        val userIdentificationAsText = userIdentificationHttpResponse.bodyAsText()

        try {
            val userIdentification = Json.decodeFromString<DiscordOAuth2UserIdentification>(userIdentificationAsText)

            dashboardWebServer.loritta.transaction {
                dashboardWebServer.updateCachedDiscordUserIdentification(userIdentification)
            }
            
            this.cachedUserIdentification = UserIdentification(
                userIdentification.id,
                userIdentification.username,
                userIdentification.discriminator,
                userIdentification.avatar,
                userIdentification.globalName,
                userIdentification.mfaEnabled,
                userIdentification.banner,
                userIdentification.accentColor,
                userIdentification.locale,
                userIdentification.email,
                userIdentification.verified,
                userIdentification.premiumType,
                userIdentification.flags,
                userIdentification.publicFlags
            )

            return userIdentification
        } catch (e: Exception) {
            logger.warn(e) { "Failed to retrieve user identification of ${userId}! Response status was ${userIdentificationHttpResponse.status} and response body was $userIdentificationAsText" }
            throw e
        }
    }

    suspend fun retrieveUserGuilds(): List<DiscordOAuth2Guild> {
        refreshTokenIfExpired()

        val userGuildsHttpResponse = loritta.http.get {
            url(dashboardWebServer.oauth2Endpoints.UserGuildsEndpoint)
            userAgent(USER_AGENT)
            header("Authorization", "Bearer ${this@UserSession.discordUserCredentials.accessToken}")
        }

        val userGuildsAsText = userGuildsHttpResponse.bodyAsText()

        try {
            val userGuilds = Json.decodeFromString<List<DiscordOAuth2Guild>>(userGuildsAsText)
            return userGuilds
        } catch (e: Exception) {
            logger.warn(e) { "Failed to retrieve user guilds of ${userId}! Response status was ${userGuildsHttpResponse.status} and response body was $userGuildsAsText" }
            throw e
        }
    }

    suspend fun refreshTokenIfExpired() {
        if (this.discordUserCredentials.isAccessTokenExpired())
            refreshToken()
    }

    suspend fun refreshToken() {
        val parameters = Parameters.build {
            append("grant_type", "refresh_token")
            append("refresh_token", this@UserSession.discordUserCredentials.refreshToken)
            append("client_id", loritta.config.loritta.discord.applicationId.toString())
            append("client_secret", loritta.config.loritta.discord.clientSecret)
        }

        val generatedAt = OffsetDateTime.now(ZoneOffset.UTC)
        val authorizationHttpResponse = loritta.http.post {
            url(dashboardWebServer.oauth2Endpoints.OAuth2TokenEndpoint)

            setBody(TextContent(parameters.formUrlEncode(), ContentType.Application.FormUrlEncoded))
        }

        val authorizationAsText = authorizationHttpResponse.bodyAsText()

        try {
            val authorization = Json.decodeFromString<DiscordOAuth2Authorization>(authorizationAsText)

            loritta.transaction {
                UserWebsiteSessions.update({ UserWebsiteSessions.token eq this@UserSession.websiteToken }) {
                    it[UserWebsiteSessions.refreshedAt] = generatedAt
                    it[UserWebsiteSessions.lastUsedAt] = generatedAt
                    it[UserWebsiteSessions.tokenType] = authorization.tokenType
                    it[UserWebsiteSessions.accessToken] = authorization.accessToken
                    it[UserWebsiteSessions.expiresIn] = authorization.expiresIn
                    it[UserWebsiteSessions.refreshToken] = authorization.refreshToken
                    it[UserWebsiteSessions.scope] = authorization.scope.split(" ")
                }
            }

            this.discordUserCredentials.accessToken = authorization.accessToken
            this.discordUserCredentials.refreshToken = authorization.refreshToken
            this.discordUserCredentials.expiresIn = authorization.expiresIn
            this.discordUserCredentials.refreshedAt = generatedAt.toInstant()
        } catch (e: Exception) {
            logger.warn(e) { "Failed to refresh the access token of ${userId}! Response status was ${authorizationHttpResponse.status} and response body was $authorizationAsText" }
            throw e
        }
    }

    data class UserIdentification(
        val id: Long,
        val username: String,
        val discriminator: String,
        val avatarId: String?,
        val globalName: String?,
        val mfaEnabled: Boolean,
        val banner: String?,
        val accentColor: Int?,
        val locale: String,
        val email: String?,
        val verified: Boolean,
        val premiumType: Int,
        val flags: Int,
        val publicFlags: Int,
    )
}