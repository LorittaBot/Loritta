package net.perfectdreams.loritta.tables.servers

import com.mrpowergamerbr.loritta.tables.ServerConfigs
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import org.jetbrains.exposed.dao.id.LongIdTable

object ServerRolePermissions : LongIdTable() {
	val guild = reference("guild", ServerConfigs).index()
	val roleId = long("role").index()

	val permission = enumeration("permission", LorittaPermission::class)
}