package net.perfectdreams.loritta.legacy.tables.servers

import net.perfectdreams.loritta.legacy.tables.ServerConfigs
import net.perfectdreams.loritta.legacy.utils.LorittaPermission
import org.jetbrains.exposed.dao.id.LongIdTable

object ServerRolePermissions : LongIdTable() {
	val guild = reference("guild", ServerConfigs).index()
	val roleId = long("role").index()

	val permission = enumeration("permission", LorittaPermission::class)
}