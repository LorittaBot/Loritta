package net.perfectdreams.loritta.cinnamon.dashboard.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging

data class LorittaJsonWebSession(
    val cachedIdentification: String?,
    val storedDiscordAuthTokens: String?
) {
    companion object {
        fun empty() = LorittaJsonWebSession(
            null,
            null
        )

        private val logger = KotlinLogging.logger {}
    }

    suspend fun getUserIdentification(loadFromCache: Boolean = true): UserIdentification? {
        if (loadFromCache) {
            try {
                cachedIdentification?.let {
                    return Json.decodeFromString(it)
                }
            } catch (e: Throwable) {
                logger.error(e) { "Error while loading cached identification" }
            }
        }

        return null
    }

    @Serializable
    data class UserIdentification(
        val id: String,
        val username: String,
        val discriminator: String,
        val verified: Boolean,
        val email: String?,
        val avatar: String?,
        val createdAt: Long,
        val updatedAt: Long
    )
}