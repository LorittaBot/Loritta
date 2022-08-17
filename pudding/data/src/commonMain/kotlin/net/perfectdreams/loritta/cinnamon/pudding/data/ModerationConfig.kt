package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.serialization.Serializable

@Serializable
data class ModerationConfig(
    val sentPunishmentViaDm: Boolean,
    val sendPunishmentToPunishLog: Boolean,
    val punishLogChannelId: Long?,
    val punishLogMessage: String?
)