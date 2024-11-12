package net.perfectdreams.loritta.serializable.config

import kotlinx.serialization.Serializable

@Serializable
data class GuildDailyShopTrinketsNotificationsConfig(
    val notifyShopTrinkets: Boolean,
    val shopTrinketsChannelId: Long?,
    val shopTrinketsMessage: String?,

    val notifyNewTrinkets: Boolean,
    val newTrinketsChannelId: Long?,
    val newTrinketsMessage: String?
)