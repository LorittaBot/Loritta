package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.serialization.Serializable

@Serializable
data class GuildProfile(
    val guildId: Long,
    val userId: Long,
    val xp: Long,
    val quickPunishment: Boolean,
    // val money: BigDecimal,
    val isInGuild: Boolean
)