package net.perfectdreams.loritta.deviousfun.cache

import dev.kord.common.entity.DiscordRole
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
data class DeviousRoleData(
    val id: Snowflake,
    val name: String,
    val color: Int,
    val hoist: Boolean,
    val icon: String?,
    val unicodeEmoji: String?,
    val position: Int,
    val permissions: Permissions,
    val managed: Boolean,
    val mentionable: Boolean,
) {
    companion object {
        fun from(role: DiscordRole): DeviousRoleData {
            return DeviousRoleData(
                role.id,
                role.name,
                role.color,
                role.hoist,
                role.icon.value,
                role.unicodeEmoji.value,
                role.position,
                role.permissions,
                role.managed,
                role.mentionable
            )
        }
    }
}