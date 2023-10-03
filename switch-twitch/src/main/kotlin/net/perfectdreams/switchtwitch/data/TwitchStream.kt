package net.perfectdreams.switchtwitch.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TwitchStream(
    val id: Long,
    @SerialName("user_id")
    val userId: Long,
    @SerialName("user_login")
    val userLogin: String,
    @SerialName("user_name")
    val userName: String,
    @SerialName("game_id")
    val gameId: Long,
    @SerialName("game_name")
    val gameName: String,
    val title: String
)