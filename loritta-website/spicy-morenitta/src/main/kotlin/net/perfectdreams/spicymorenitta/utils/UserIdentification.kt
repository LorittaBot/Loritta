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
        val avatar: String?,
        @SerialName("bot")
        val bot: Boolean?,
        @SerialName("mfa_enabled")
        @Optional val mfaEnabled: Boolean? = false,
        @SerialName("locale")
        val locale: String?,
        @SerialName("verified")
        val verified: Boolean?,
        @SerialName("email")
        val email: String?,
        @SerialName("flags")
        val flags: Int?,
        @SerialName("premium_type")
        @Optional val premiumType: Int? = 0
)