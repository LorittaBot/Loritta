package net.perfectdreams.dora.tables

import net.perfectdreams.dora.PermissionLevel

object Users : UniqueSnowflakeTable() {
    val permissionLevel = enumerationByName<PermissionLevel>("permission_level", 64)
}