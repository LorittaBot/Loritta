package net.perfectdreams.loritta.morenitta.tables.servers

import net.perfectdreams.loritta.morenitta.tables.ServerConfigs
import net.perfectdreams.loritta.morenitta.utils.LorittaPermission
import org.jetbrains.exposed.dao.id.LongIdTable

object ServerRolePermissions : LongIdTable() {
    val guild = reference("guild", ServerConfigs).index()
    val roleId = long("role").index()

    val permission = enumeration("permission", LorittaPermission::class)
}