package net.perfectdreams.loritta.cinnamon.dashboard.backend.utils

import dev.kord.common.entity.DiscordUser
import io.ktor.server.application.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend

class LorittaWebSession(val m: LorittaDashboardBackend, val jsonWebSession: LorittaJsonWebSession) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun getUserIdentification(call: ApplicationCall, loadFromCache: Boolean = true): LorittaJsonWebSession.UserIdentification? {
        if (loadFromCache) {
            try {
                jsonWebSession.cachedIdentification?.let {
                    return Json.decodeFromString(it)
                }
            } catch (e: Throwable) {
                logger.error(e) { "Error while loading cached identification" }
            }
        }

        val discordIdentification = getDiscordAuthFromJson() ?: return null

        return try {
            val userIdentification = discordIdentification.getUserIdentification()
            val forCache = convertToWebSessionIdentification(userIdentification)

            call.lorittaSession = jsonWebSession.copy(
                cachedIdentification = convertToJson(discordIdentification)
            )

            forCache
        } catch (e: Exception) {
            null
        }
    }

    fun getDiscordAuthFromJson(): TemmieDiscordAuth? {
        if (jsonWebSession.storedDiscordAuthTokens == null)
            return null

        val json = Json.decodeFromString<LorittaJsonWebSession.StoredDiscordAuthTokens>(jsonWebSession.storedDiscordAuthTokens)

        return TemmieDiscordAuth(
            "x",
            "y",
            json.authCode,
            json.redirectUri,
            json.scope,
            json.accessToken,
            json.refreshToken,
            json.expiresIn,
            json.generatedAt
        )
    }

    private fun convertToWebSessionIdentification(discordUser: DiscordUser): LorittaJsonWebSession.UserIdentification {
        val now = System.currentTimeMillis()

        return LorittaJsonWebSession.UserIdentification(
            discordUser.id.value.toString(),
            discordUser.username,
            discordUser.discriminator,
            discordUser.verified.discordBoolean,
            discordUser.email.value,
            discordUser.avatar,
            now,
            now
        )
    }

    private fun convertToJson(discordAuth: TemmieDiscordAuth): String {
        return Json.encodeToString(
            LorittaJsonWebSession.StoredDiscordAuthTokens(
                discordAuth.authCode,
                discordAuth.redirectUri,
                discordAuth.scope,
                discordAuth.accessToken,
                discordAuth.refreshToken,
                discordAuth.expiresIn,
                discordAuth.generatedAt
            )
        )
    }
}