package net.perfectdreams.loritta.morenitta.websitedashboard.discord

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class DiscordOAuth2Authorization(
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("expires_in")
    val expiresIn: Long,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("scope")
    val scope: String,
)