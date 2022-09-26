package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.sonhosrank

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.data.SingleUserComponentData

@Serializable
data class ChangeSonhosRankPageData(
    override val userId: Snowflake,
    val page: Long,
    val rankType: SonhosRankType
) : SingleUserComponentData