package net.perfectdreams.loritta.temmiewebsession

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import io.ktor.server.application.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import java.util.*

data class LorittaJsonWebSession(
    val base64CachedIdentification: String?,
    val base64StoredDiscordAuthTokens: String?
) {
    companion object {
        fun empty() = LorittaJsonWebSession(
            null,
            null
        )

        private val logger = KotlinLogging.logger {}
    }

    suspend fun getUserIdentification(applicationId: Long, clientSecret: String, call: ApplicationCall): UserIdentification? = getUserIdentification(applicationId.toString(), clientSecret, call)

    suspend fun getUserIdentification(applicationId: String, clientSecret: String, call: ApplicationCall, loadFromCache: Boolean = true): UserIdentification? {
        if (loadFromCache) {
            try {
                base64CachedIdentification?.let {
                    return Json.decodeFromString(Base64.getDecoder().decode(it.toByteArray(Charsets.UTF_8)).toString(Charsets.UTF_8))
                }
            } catch (e: Throwable) {
                logger.warn(e) { "Failed to load cached identification! Ignoring cached identification..." }
            }
        }

        val discordIdentification = getDiscordAuth(applicationId, clientSecret, call) ?: return null

        return try {
            val userIdentification = discordIdentification.getUserIdentification()
            val forCache = LorittaWebSession.convertToWebSessionIdentification(userIdentification)

            call.lorittaSession = this.copy(
                base64CachedIdentification = Base64.getEncoder().encode(forCache.toJson().toByteArray(Charsets.UTF_8)).toString(Charsets.UTF_8)
            )

            forCache
        } catch (e: Exception) {
            logger.warn(e) { "Failed to get user identification!" }
            null
        }
    }

    fun getDiscordAuth(applicationId: Long, clientSecret: String, call: ApplicationCall): TemmieDiscordAuth? = getDiscordAuth(applicationId.toString(), clientSecret, call)

    fun getDiscordAuth(applicationId: String, clientSecret: String, call: ApplicationCall): TemmieDiscordAuth? {
        if (base64StoredDiscordAuthTokens == null)
            return null

        val json = try {
            JsonParser.parseString(Base64.getDecoder().decode(base64StoredDiscordAuthTokens.toByteArray(Charsets.UTF_8)).toString(Charsets.UTF_8))
        } catch (e: Exception) {
            logger.error(e) { "Error while loading cached discord auth" }
            return null
        }

        return LorittaTemmieDiscordAuth(
            call,
            applicationId,
            clientSecret,
            json["authCode"].string,
            json["redirectUri"].string,
            json["scope"].array.map { it.string },
            json["accessToken"].string,
            json["refreshToken"].string,
            json["expiresIn"].long,
            json["generatedAt"].long
        )
    }

    suspend fun getUserIdentificationAndDiscordAuth(applicationId: Long, clientSecret: String, call: ApplicationCall): Pair<UserIdentification?, TemmieDiscordAuth?> = getUserIdentificationAndDiscordAuth(applicationId.toString(), clientSecret, call)

    suspend fun getUserIdentificationAndDiscordAuth(applicationId: String, clientSecret: String, call: ApplicationCall): Pair<UserIdentification?, TemmieDiscordAuth?> = Pair(getUserIdentification(applicationId, clientSecret, call), getDiscordAuth(applicationId, clientSecret, call))

    private fun convertToJson(discordAuth: TemmieDiscordAuth): String {
        return Json.encodeToString(
            StoredDiscordAuthTokens(
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

    @Serializable
    data class UserIdentification(
        val id: String,
        val username: String,
        val discriminator: String,
        val verified: Boolean,
        val globalName: String? = null,
        val email: String? = null, // Looks like this can be missing
        val avatar: String? = null, // Looks like this can be missing
        val createdAt: Long,
        val updatedAt: Long
    ) {
        fun toJson() = Json.encodeToString(this)

        // TODO - htmx-mix: Refactor this!
        val effectiveAvatarUrl: String
            get() {
                val avatarId = avatar
                val url = if (avatarId != null) {
                    "https://cdn.discordapp.com/avatars/${id}/${avatarId}.png"
                } else {
                    "https://cdn.discordapp.com/embed/avatars/${(id.toLong() shr 22) % 6}.png"
                }

                return url
            }
    }

    @Serializable
    data class StoredDiscordAuthTokens(
        val authCode: String? = null,
        val redirectUri: String,
        val scope: List<String>,
        val accessToken: String? = null,
        val refreshToken: String? = null,
        val expiresIn: Long? = null,
        val generatedAt: Long? = null
    )
}