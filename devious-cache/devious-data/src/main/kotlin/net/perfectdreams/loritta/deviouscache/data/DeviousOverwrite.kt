package net.perfectdreams.loritta.deviouscache.data

import dev.kord.common.entity.Overwrite
import dev.kord.common.entity.OverwriteType
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
public data class DeviousOverwrite(
    val id: LightweightSnowflake,
    val type: OverwriteType,
    val allow: LightweightPermissions,
    val deny: LightweightPermissions,
) {
    fun toKordOverwrite() = Overwrite(
        id.toKordSnowflake(),
        type,
        Permissions(allow.code.value),
        Permissions(deny.code.value)
    )
}