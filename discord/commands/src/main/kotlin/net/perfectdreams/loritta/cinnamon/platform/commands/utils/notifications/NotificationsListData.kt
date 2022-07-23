package net.perfectdreams.loritta.cinnamon.platform.commands.utils.notifications

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.platform.components.data.SingleUserComponentData

@Serializable
data class NotificationsListData(
    override val userId: Snowflake,
    val page: Long
) : SingleUserComponentData