package net.perfectdreams.loritta.morenitta.websitedashboard.discord

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class DiscordOAuth2UserIdentification(
    val id: Long,
    val username: String,
    val discriminator: String,
    val avatar: String?,
    @SerialName("global_name")
    val globalName: String?,
    @SerialName("mfa_enabled")
    val mfaEnabled: Boolean,
    val banner: String?,
    @SerialName("accent_color")
    val accentColor: Int?,
    val locale: String,
    val email: String?,
    val verified: Boolean,
    @SerialName("premium_type")
    val premiumType: Int,
    val flags: Int,
    @SerialName("public_flags")
    val publicFlags: Int,
)