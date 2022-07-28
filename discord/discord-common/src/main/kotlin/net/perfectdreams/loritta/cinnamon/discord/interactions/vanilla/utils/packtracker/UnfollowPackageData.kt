package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.packtracker

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.data.SingleUserComponentData

@Serializable
data class UnfollowPackageData(
    override val userId: Snowflake,
    val trackingId: String
) : SingleUserComponentData