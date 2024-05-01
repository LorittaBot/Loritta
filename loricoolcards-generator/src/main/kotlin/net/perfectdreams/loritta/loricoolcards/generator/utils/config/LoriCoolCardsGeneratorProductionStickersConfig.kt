package net.perfectdreams.loritta.loricoolcards.generator.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class LoriCoolCardsGeneratorProductionStickersConfig(
    val botToken: String,
    val guildIdToPreCacheUsersFrom: Long,
    val pudding: PuddingConfig,
    val dreamStorageService: DreamStorageServiceConfig,
) {
    @Serializable
    data class PuddingConfig(
        val database: String,
        val address: String,
        val username: String,
        val password: String
    )

    @Serializable
    data class DreamStorageServiceConfig(
        val url: String,
        val token: String
    )
}