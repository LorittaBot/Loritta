package net.perfectdreams.spicymorenitta.utils

import kotlinx.serialization.Optional
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
        @Optional val avatar: String? = null,
        @SerialName("bot")
        @Optional val bot: Boolean? = false,
        @SerialName("mfa_enabled")
        @Optional val mfaEnabled: Boolean? = false,
        @SerialName("locale")
        @Optional val locale: String? = null,
        @SerialName("verified")
        @Optional val verified: Boolean? = null,
        @SerialName("email")
        @Optional val email: String? = null,
        @SerialName("flags")
        @Optional val flags: Int? = 0,
        @SerialName("premium_type")
        @Optional val premiumType: Int? = 0
) {
        val userAvatarUrl: String
                get() {
                        val extension = if (avatar?.startsWith("a_") == true) { // Avatares animados no Discord começam com "_a"
                                "gif"
                        } else { "png" }

                        return "https://cdn.discordapp.com/avatars/${id}/${avatar}.${extension}?size=256"
                }
}