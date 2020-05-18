package net.perfectdreams.spicymorenitta.utils

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class UserIdentification(
        @SerialName("id")
        val id: String,
        @SerialName("username")
        val username: String,
        @SerialName("discriminator")
        val discriminator: String,
        @SerialName("avatar")
        val avatar: String? = null,
        @SerialName("bot")
        val bot: Boolean? = false,
        @SerialName("mfa_enabled")
        val mfaEnabled: Boolean? = false,
        @SerialName("locale")
        val locale: String? = null,
        @SerialName("verified")
        val verified: Boolean? = null,
        @SerialName("email")
        val email: String? = null,
        @SerialName("flags")
        val flags: Int? = 0,
        @SerialName("premium_type")
        val premiumType: Int? = 0
) {
        val userAvatarUrl: String
                get() {
                        val extension = if (avatar?.startsWith("a_") == true) { // Avatares animados no Discord começam com "_a"
                                "gif"
                        } else { "png" }

                        return "https://cdn.discordapp.com/avatars/${id}/${avatar}.${extension}?size=256"
                }
}