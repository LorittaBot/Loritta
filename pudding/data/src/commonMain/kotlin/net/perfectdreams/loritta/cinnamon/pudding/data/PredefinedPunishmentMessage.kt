package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.serialization.Serializable

@Serializable
data class PredefinedPunishmentMessage(
    val short: String,
    val message: String
)