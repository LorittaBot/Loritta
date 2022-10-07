package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import net.perfectdreams.loritta.deviousfun.JDA
import net.perfectdreams.loritta.deviousfun.cache.DeviousRoleData
import java.awt.Color

class Role(val jda: JDA, val guild: Guild, val role: DeviousRoleData) : IdentifiableSnowflake {
    override val idSnowflake: Snowflake
        get() = role.id
    val name: String
        get() = role.name
    val asMention: String
        get() = "<@&${idSnowflake}>"
    val colorRaw: Int
        get() = role.color
    val color = if (colorRaw != 0) Color(colorRaw) else null
    val isHoisted: Boolean
        get() = role.hoist
    val isManaged: Boolean
        get() = role.managed
    val isPublicRole: Boolean
        get() = guild.idSnowflake == idSnowflake
    val isMentionable: Boolean
        get() = role.mentionable
    val position: Int
        get() = role.position
    val positionRaw: Int
        get() = role.position
    val permissions: Permissions
        get() = role.permissions

    override fun equals(other: Any?): Boolean {
        if (other !is Role)
            return false

        return this.guild.idSnowflake == other.guild.idSnowflake && this.idSnowflake == other.idSnowflake
    }
}