package net.perfectdreams.akinatorreapi.payload

import kotlinx.serialization.SerialInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameIdentification(
    val channel: Int,
    val session: String,
    val signature: String,
    @SerialName("challenge_auth")
    val challengeAuth: String
)