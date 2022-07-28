package net.perfectdreams.loritta.cinnamon.discord.interactions.commands

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.data.SingleUserComponentData

/**
 * A barebones implementation of [SingleUserComponentData]
 */
@Serializable
data class BarebonesSingleUserComponentData(override val userId: Snowflake) : SingleUserComponentData