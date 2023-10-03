package net.perfectdreams.switchtwitch.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionCreateResponse(
    val total: Int,
    val data: List<SubscriptionData>,
    @SerialName("max_total_cost")
    val maxTotalCost: Int,
    @SerialName("total_cost")
    val totalCost: Int
)