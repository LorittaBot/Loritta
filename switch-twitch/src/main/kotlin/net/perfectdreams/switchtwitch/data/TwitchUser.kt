package net.perfectdreams.switchtwitch.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TwitchUser(
    val id: Long,
    val login: String,
    val description: String,
    @SerialName("broadcaster_type")
    val broadcasterType: String,
    @SerialName("profile_image_url")
    val profileImageUrl: String,
    @SerialName("offline_image_url")
    val offlineImageUrl: String,
    @SerialName("display_name")
    val displayName: String

)