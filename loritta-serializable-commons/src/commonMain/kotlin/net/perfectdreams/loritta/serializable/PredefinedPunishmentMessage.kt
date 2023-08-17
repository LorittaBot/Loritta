package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
data class PredefinedPunishmentMessage(
    val short: String,
    val message: String
)