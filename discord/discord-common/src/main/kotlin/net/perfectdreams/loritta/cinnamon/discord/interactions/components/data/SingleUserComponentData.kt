package net.perfectdreams.loritta.cinnamon.discord.interactions.components.data

import dev.kord.common.entity.Snowflake

/**
 * A component data that has a User ID, useful for components that should only be available for a specific user
 */
interface SingleUserComponentData : ComponentData {
    val userId: Snowflake
}