package net.perfectdreams.switchtwitch.data

import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionCreateRequest(
    val type: String,
    val version: String,
    val condition: Map<String, String>,
    val transport: SubTransportCreate
)