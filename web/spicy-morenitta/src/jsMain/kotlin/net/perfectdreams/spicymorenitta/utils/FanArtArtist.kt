package net.perfectdreams.spicymorenitta.utils

import kotlinx.serialization.Serializable

@Serializable
data class FanArtArtist constructor(
        val id: String,
        val aboutMe: String? = null,
        val info: Info,
        val user: User? = null,
        val fanArts: List<FanArt>
) {
    @Serializable
    data class Info(
            val name: String?,
            val avatarUrl: String?,
            val override: Info? = null
    )

    @Serializable
    data class User(
            val name: String,
            val effectiveAvatarUrl: String
    )
}