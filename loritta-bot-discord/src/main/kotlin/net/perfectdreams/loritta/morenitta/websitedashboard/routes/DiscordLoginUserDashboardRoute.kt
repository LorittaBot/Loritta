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
import io.ktor.util.date.GMTDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import kotlinx.serialization.Serializable
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserWebsiteSessions
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.Base58
import net.perfectdreams.loritta.morenitta.website.routes.LocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.insert
import java.time.OffsetDateTime
import java.time.ZoneOffset

class DiscordLoginUserDashboardRoute(val website: LorittaDashboardWebServer) : BaseRoute("/discord/login") {
    private val PREFIX = "https://discord.com/api/v10"
    private val TOKEN_BASE_URL = "$PREFIX/oauth2/token"
    private val USER_AGENT = "Loritta-Morenitta-Discord-Auth/1.0"
    private val USER_IDENTIFICATION_URL = "${PREFIX}/users/@me"
    private val USER_GUILDS_URL = "$USER_IDENTIFICATION_URL/guilds"

    override suspend fun onRequest(call: ApplicationCall) {
        val accessCode = call.request.queryParameters["code"]
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
            url(TOKEN_BASE_URL)
            userAgent(USER_AGENT)

            setBody(TextContent(parameters.formUrlEncode(), ContentType.Application.FormUrlEncoded))
        }.bodyAsText()

        val result = Json.decodeFromString<DiscordResult>(resultAsText)

        // We also want to get the user's information and associate it with the session
        // This way, we can avoid doing round trips every time when sending requests "on behalf" of the user
        // Because we already have the user's IDs!
        val userIdentificationAsText = website.loritta.http.get {
            url(USER_IDENTIFICATION_URL)
            userAgent(USER_AGENT)

            header("Authorization", "Bearer ${result.accessToken}")
        }.bodyAsText()

        val userIdentification = Json.decodeFromString<UserIdentification>(userIdentificationAsText)

        // We now need to query the user's user ID
        val createdAt = OffsetDateTime.now(ZoneOffset.UTC)

        val tokenAsBytes = ByteArray(64)
        website.loritta.random.nextBytes(tokenAsBytes)
        val token = Base58.encode(tokenAsBytes)

        website.loritta.transaction {
            UserWebsiteSessions.insert {
                it[UserWebsiteSessions.token] = token
                it[UserWebsiteSessions.userId] = userIdentification.id
                it[UserWebsiteSessions.createdAt] = createdAt
                it[UserWebsiteSessions.refreshedAt] = createdAt
                it[UserWebsiteSessions.lastUsedAt] = createdAt
                it[UserWebsiteSessions.tokenType] = result.tokenType
                it[UserWebsiteSessions.accessToken] = result.accessToken
                it[UserWebsiteSessions.expiresIn] = result.expiresIn
                it[UserWebsiteSessions.refreshToken] = result.refreshToken
                it[UserWebsiteSessions.scope] = result.scope.split(" ")

                it[UserWebsiteSessions.username] = userIdentification.username
                it[UserWebsiteSessions.globalName] = userIdentification.globalName
                it[UserWebsiteSessions.discriminator] = userIdentification.discriminator
                it[UserWebsiteSessions.avatarId] = userIdentification.avatar
            }
        }

        call.response.cookies.append(
            LorittaDashboardWebServer.WEBSITE_SESSION_COOKIE,
            token,
            path = "/", // Available in any path of the domain
            domain = website.loritta.config.loritta.dashboard.cookieDomain,
            // secure = true, // Only sent via HTTPS
            httpOnly = true // Disable JS access
        )

        val i18nContext = website.getI18nContextFromCall(call)

        // Successfully authenticated, let's freaking go!!!
        call.respondRedirect("/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/", false)
    }

    @Serializable
    data class DiscordResult(
        @SerialName("token_type")
        val tokenType: String,
        @SerialName("access_token")
        val accessToken: String,
        @SerialName("expires_in")
        val expiresIn: Long,
        @SerialName("refresh_token")
        val refreshToken: String,
        @SerialName("scope")
        val scope: String,
    )

    @Serializable
    data class DiscordGuild(
        val id: Long,
        val name: String,
        val icon: String?,
        val banner: String?,
        val owner: Boolean,
        val permissions: Long,
        val features: List<String>,
    )

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    @JsonIgnoreUnknownKeys
    data class UserIdentification(
        val id: Long,
        val username: String,
        val discriminator: String,
        val avatar: String?,
        @SerialName("global_name")
        val globalName: String?,
        @SerialName("mfa_enabled")
        val mfaEnabled: Boolean?,
        val locale: String,
        @SerialName("premium_type")
        val premiumType: Int,
        val flags: Int,
        val email: String?,
        val verified: Boolean,
    )
}