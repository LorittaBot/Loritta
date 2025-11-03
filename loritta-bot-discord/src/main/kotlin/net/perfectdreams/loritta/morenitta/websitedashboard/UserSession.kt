package net.perfectdreams.loritta.morenitta.websitedashboard

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.userAgent
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserFavoritedGuilds
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.DiscordLoginUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.DiscordLoginUserDashboardRoute.UserIdentification
import org.jetbrains.exposed.sql.selectAll

data class UserSession(
    val websiteToken: String,
    val discordAccessToken: String,
    val userId: Long,
    val username: String,
    val discriminator: String,
    val globalName: String?,
    val avatarId: String?
) {
    private val PREFIX = "https://discord.com/api/v10"
    private val TOKEN_BASE_URL = "$PREFIX/oauth2/token"
    private val USER_AGENT = "Loritta-Morenitta-Discord-Auth/1.0"
    private val USER_IDENTIFICATION_URL = "${PREFIX}/users/@me"
    private val USER_GUILDS_URL = "$USER_IDENTIFICATION_URL/guilds"

    fun getEffectiveAvatarUrl(): String {
        val userAvatarId = this.avatarId

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

    suspend fun getUserIdentification(loritta: LorittaBot): UserIdentification {
        val userIdentificationAsText = loritta.http.get {
            url(USER_IDENTIFICATION_URL)
            userAgent(USER_AGENT)

            header("Authorization", "Bearer ${discordAccessToken}")
        }.bodyAsText()

        val userIdentification = Json.decodeFromString<UserIdentification>(userIdentificationAsText)
        return userIdentification
    }

    suspend fun getUserGuilds(loritta: LorittaBot): List<DiscordLoginUserDashboardRoute.DiscordGuild> {
        val resultAsText = loritta.http.get {
            url(USER_GUILDS_URL)
            userAgent(USER_AGENT)
            header("Authorization", "Bearer $discordAccessToken")
        }.bodyAsText()

        val favoritedGuilds = loritta.transaction {
            UserFavoritedGuilds.selectAll()
                .where {
                    UserFavoritedGuilds.userId eq userId
                }
                .map { it[UserFavoritedGuilds.guildId] }
                .toSet()
        }

        val userGuilds = Json.decodeFromString<List<DiscordLoginUserDashboardRoute.DiscordGuild>>(resultAsText)
        return userGuilds
    }
}