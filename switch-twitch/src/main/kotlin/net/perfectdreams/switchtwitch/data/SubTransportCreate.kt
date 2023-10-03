package net.perfectdreams.switchtwitch.data

import kotlinx.serialization.Serializable

@Serializable
data class SubTransportCreate(
    val method: String,
    val callback: String,
    val secret: String
)