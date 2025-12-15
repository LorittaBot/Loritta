package net.perfectdreams.loritta.morenitta.websitedashboard.discord

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class DiscordOAuth2Guild(
    val id: Long,
    val name: String,
    val icon: String?,
    val banner: String?,
    val owner: Boolean,
    val permissions: Long,
    val features: List<String>,
)