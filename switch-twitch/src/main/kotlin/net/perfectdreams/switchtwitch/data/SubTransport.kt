package net.perfectdreams.switchtwitch.data

import kotlinx.serialization.Serializable

@Serializable
data class SubTransport(
    val method: String,
    val callback: String
)