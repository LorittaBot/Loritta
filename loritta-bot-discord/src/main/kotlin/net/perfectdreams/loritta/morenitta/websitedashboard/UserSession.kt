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

        val userIdentificationAsText = loritta.http.get {
            url(dashboardWebServer.oauth2Endpoints.UserIdentificationEndpoint)
            userAgent(USER_AGENT)

            header("Authorization", "Bearer ${this@UserSession.discordUserCredentials.accessToken}")
        }.bodyAsText()

        val userIdentification = Json.decodeFromString<DiscordOAuth2UserIdentification>(userIdentificationAsText)
        return userIdentification
    }

    suspend fun retrieveUserGuilds(): List<DiscordOAuth2Guild> {
        refreshTokenIfExpired()

        val resultAsText = loritta.http.get {
            url(dashboardWebServer.oauth2Endpoints.UserGuildsEndpoint)
            userAgent(USER_AGENT)
            header("Authorization", "Bearer ${this@UserSession.discordUserCredentials.accessToken}")
        }.bodyAsText()

        val userGuilds = Json.decodeFromString<List<DiscordOAuth2Guild>>(resultAsText)
        return userGuilds
    }

    suspend fun refreshTokenIfExpired() {
        if (this.discordUserCredentials.isAccessTokenExpired())
            refreshToken()
    }

    suspend fun refreshToken() {
        val basic = "Basic " + Base64.getEncoder().encodeToString("${loritta.config.loritta.discord.applicationId}:${loritta.config.loritta.discord.clientSecret}".toByteArray())

        val parameters = Parameters.build {
            append("grant_type", "refresh_token")
            append("refresh_token", this@UserSession.discordUserCredentials.refreshToken)
        }

        val generatedAt = OffsetDateTime.now(ZoneOffset.UTC)
        val authorizationHttpResponse = loritta.http.post {
            url(dashboardWebServer.oauth2Endpoints.OAuth2TokenEndpoint)
            header("Authorization", basic)

            setBody(TextContent(parameters.formUrlEncode(), ContentType.Application.FormUrlEncoded))
        }

        val autorization = Json.decodeFromString<DiscordOAuth2Authorization>(authorizationHttpResponse.bodyAsText())

        loritta.transaction {
            UserWebsiteSessions.update({ UserWebsiteSessions.token eq this@UserSession.websiteToken }) {
                it[UserWebsiteSessions.refreshedAt] = generatedAt
                it[UserWebsiteSessions.lastUsedAt] = generatedAt
                it[UserWebsiteSessions.tokenType] = autorization.tokenType
                it[UserWebsiteSessions.accessToken] = autorization.accessToken
                it[UserWebsiteSessions.expiresIn] = autorization.expiresIn
                it[UserWebsiteSessions.refreshToken] = autorization.refreshToken
                it[UserWebsiteSessions.scope] = autorization.scope.split(" ")
            }
        }

        this.discordUserCredentials.accessToken = autorization.accessToken
        this.discordUserCredentials.refreshToken = autorization.refreshToken
        this.discordUserCredentials.expiresIn = autorization.expiresIn
        this.discordUserCredentials.refreshedAt = generatedAt.toInstant()
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