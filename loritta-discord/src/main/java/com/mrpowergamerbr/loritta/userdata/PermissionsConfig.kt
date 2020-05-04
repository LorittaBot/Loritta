package com.mrpowergamerbr.loritta.userdata

import com.mrpowergamerbr.loritta.utils.LorittaPermission

@Deprecated("Migrated to PostgreSQL")
class PermissionsConfig {
	var roles = mutableMapOf<String, PermissionRole>()

	class PermissionRole {
		var permissions: MutableList<LorittaPermission> = mutableListOf<LorittaPermission>()
	}
}