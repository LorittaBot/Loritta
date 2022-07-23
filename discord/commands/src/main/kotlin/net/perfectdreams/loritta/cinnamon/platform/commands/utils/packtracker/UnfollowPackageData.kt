package net.perfectdreams.loritta.cinnamon.platform.commands.utils.packtracker

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.platform.components.data.SingleUserComponentData

@Serializable
data class UnfollowPackageData(
    override val userId: Snowflake,
    val trackingId: String
) : SingleUserComponentData