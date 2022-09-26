package net.perfectdreams.loritta.legacy.serializable

import kotlinx.serialization.Serializable

@Serializable
data class PaymentScoreboardEntry(
        val money: Double,
        val user: DiscordUser
)