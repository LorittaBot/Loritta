package net.perfectdreams.loritta.morenitta.websitedashboard

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.content.*
import io.ktor.http.formUrlEncode
import io.ktor.http.userAgent
import kotlinx.serialization.json.Json
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.morenitta.websitedashboard.discord.DiscordOAuth2Authorization
import net.perfectdreams.loritta.morenitta.websitedashboard.discord.DiscordOAuth2Guild
import net.perfectdreams.loritta.morenitta.websitedashboard.discord.DiscordOAuth2UserIdentification
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.text.startsWith

abstract class UserSession(
    val oauth2Manager: DiscordOAuth2Manager,
    val applicationId: Long,
    val clientSecret: String,
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

        val userIdentificationHttpResponse = oauth2Manager.http.get {
            url(oauth2Manager.oauth2Endpoints.UserIdentificationEndpoint)
            userAgent(USER_AGENT)

            header("Authorization", "Bearer ${this@UserSession.discordUserCredentials.accessToken}")
        }

        if (userIdentificationHttpResponse.status == HttpStatusCode.Unauthorized)
            throw UnauthorizedTokenException()

        val userIdentificationAsText = userIdentificationHttpResponse.bodyAsText()

        try {
            val userIdentification = Json.decodeFromString<DiscordOAuth2UserIdentification>(userIdentificationAsText)

            updateCachedUserInfoExternally(userIdentification)

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

        val userGuildsHttpResponse = oauth2Manager.http.get {
            url(oauth2Manager.oauth2Endpoints.UserGuildsEndpoint)
            userAgent(USER_AGENT)
            header("Authorization", "Bearer ${this@UserSession.discordUserCredentials.accessToken}")
        }

        if (userGuildsHttpResponse.status == HttpStatusCode.Unauthorized)
            throw UnauthorizedTokenException()

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
            append("client_id", applicationId.toString())
            append("client_secret", clientSecret)
        }

        val generatedAt = OffsetDateTime.now(ZoneOffset.UTC)
        val authorizationHttpResponse = oauth2Manager.http.post {
            url(oauth2Manager.oauth2Endpoints.OAuth2TokenEndpoint)

            setBody(TextContent(parameters.formUrlEncode(), ContentType.Application.FormUrlEncoded))
        }

        // If the refresh token is invalid, Discord will throw an invalid_grant (bad request)
        if (authorizationHttpResponse.status == HttpStatusCode.BadRequest)
            throw UnauthorizedTokenException()

        val authorizationAsText = authorizationHttpResponse.bodyAsText()

        try {
            val authorization = Json.decodeFromString<DiscordOAuth2Authorization>(authorizationAsText)

            updateWebsiteSessionExternally(generatedAt, authorization)

            this.discordUserCredentials.accessToken = authorization.accessToken
            this.discordUserCredentials.refreshToken = authorization.refreshToken
            this.discordUserCredentials.expiresIn = authorization.expiresIn
            this.discordUserCredentials.refreshedAt = generatedAt.toInstant()
        } catch (e: Exception) {
            logger.warn(e) { "Failed to refresh the access token of ${userId}! Response status was ${authorizationHttpResponse.status} and response body was $authorizationAsText" }
            throw e
        }
    }

    abstract suspend fun updateCachedUserInfoExternally(userIdentification: DiscordOAuth2UserIdentification)
    abstract suspend fun updateWebsiteSessionExternally(generatedAt: OffsetDateTime, authorization: DiscordOAuth2Authorization)

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