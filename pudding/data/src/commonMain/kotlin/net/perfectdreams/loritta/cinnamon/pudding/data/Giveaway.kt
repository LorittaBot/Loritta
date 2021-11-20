package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.serialization.Serializable

@Serializable
data class Giveaway(
    val messageId: Long,
    val channelId: Long,
    val guildId: Long,
    val title: String,
    val numberOfWinners: Int,
    val users: Array<String>,
    val finishAt: Long,
    val finished: Boolean,
    val host: Long,
    val awardRoleIds: Array<String>?,
    val awardSonhosPerWinner: Long?
)