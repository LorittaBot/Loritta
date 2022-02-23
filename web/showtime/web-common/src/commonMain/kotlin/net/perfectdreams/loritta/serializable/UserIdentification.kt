package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
data class UserIdentification(
        val id: Long,
        val username: String,
        val discriminator: String,
        val avatar: String? = null,
        val bot: Boolean? = false,
        val mfaEnabled: Boolean? = false,
        val locale: String? = null,
        val verified: Boolean? = null,
        val email: String? = null,
        val flags: Int? = 0,
        val premiumType: Int? = 0
) {
    val userAvatarUrl: String
        get() {
            val extension = if (avatar?.startsWith("a_") == true) { // Avatares animados no Discord começam com "_a"
                "gif"
            } else { "png" }

            return "https://cdn.discordapp.com/avatars/${id}/${avatar}.${extension}?size=256"
        }

    val effectiveAvatarUrl: String
        get() {
            return if (avatar != null) {
                val extension = if (avatar.startsWith("a_")) { // Avatares animados no Discord começam com "_a"
                    "gif"
                } else {
                    "png"
                }

                "https://cdn.discordapp.com/avatars/$id/${avatar}.${extension}?size=256"
            } else {
                val avatarId = id % 5

                "https://cdn.discordapp.com/embed/avatars/$avatarId.png?size=256"
            }
        }
}