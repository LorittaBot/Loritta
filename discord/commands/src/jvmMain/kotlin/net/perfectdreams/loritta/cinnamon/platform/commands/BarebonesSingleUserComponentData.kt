package net.perfectdreams.loritta.cinnamon.platform.commands

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.platform.components.data.SingleUserComponentData

/**
 * A barebones implementation of [SingleUserComponentData]
 */
@Serializable
data class BarebonesSingleUserComponentData(override val userId: Snowflake) : SingleUserComponentData