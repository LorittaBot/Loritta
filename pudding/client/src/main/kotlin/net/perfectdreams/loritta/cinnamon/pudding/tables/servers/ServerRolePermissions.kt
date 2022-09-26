package net.perfectdreams.loritta.cinnamon.pudding.tables.servers

import net.perfectdreams.loritta.cinnamon.utils.LorittaPermission
import org.jetbrains.exposed.dao.id.LongIdTable

object ServerRolePermissions : LongIdTable() {
    val guild = reference("guild", ServerConfigs).index()
    val roleId = long("role").index()

    val permission = enumeration("permission", LorittaPermission::class)
}