package net.perfectdreams.loritta.cinnamon.platform.commands.fortnite

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.platform.components.data.SingleUserComponentData

@Serializable
data class ChangeFortniteBattleRoyaleNewsPageData(
    override val userId: Snowflake,
    val hash: String
) : SingleUserComponentData