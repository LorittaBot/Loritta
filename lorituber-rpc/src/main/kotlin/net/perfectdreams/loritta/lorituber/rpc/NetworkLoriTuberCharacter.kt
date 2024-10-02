package net.perfectdreams.loritta.lorituber.rpc

import kotlinx.serialization.Serializable

@Serializable
data class NetworkLoriTuberCharacter(
    val id: Long,
    val name: String
)