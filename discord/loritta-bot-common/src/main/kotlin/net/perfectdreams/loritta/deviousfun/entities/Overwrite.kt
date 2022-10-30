package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.Overwrite
import dev.kord.common.entity.OverwriteType
import dev.kord.rest.builder.channel.ChannelPermissionModifyBuilder
import dev.kord.rest.service.editRolePermission
import net.perfectdreams.loritta.deviousfun.DeviousFun

class Overwrite(val deviousFun: DeviousFun, val channel: Channel, val overwrite: Overwrite) {
    val id by overwrite::id
    val deny by overwrite::deny
    val allow by overwrite::allow
    val type by overwrite::type

    suspend fun edit(builder: ChannelPermissionModifyBuilder.() -> (Unit)) {
        when (overwrite.type) {
            OverwriteType.Member -> TODO()
            OverwriteType.Role -> deviousFun.loritta.rest.channel.editRolePermission(
                channel.idSnowflake,
                overwrite.id,
                builder
            )

            is OverwriteType.Unknown -> TODO()
        }
    }
}