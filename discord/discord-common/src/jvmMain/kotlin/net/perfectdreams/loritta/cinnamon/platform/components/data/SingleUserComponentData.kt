package net.perfectdreams.loritta.cinnamon.platform.components.data

import dev.kord.common.entity.Snowflake

/**
 * A component data that has a User ID, useful for components that should only be available for a specific user
 */
interface SingleUserComponentData {
    val userId: Snowflake
}