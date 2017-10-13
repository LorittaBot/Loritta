package com.mrpowergamerbr.loritta.userdata

import com.mrpowergamerbr.loritta.utils.LorittaPermission

class PermissionsConfig(
		var roles: MutableMap<String, PermissionRole> = mutableMapOf<String, PermissionRole>()
) {
	class PermissionRole() {
		var permissions: MutableList<LorittaPermission> = mutableListOf<LorittaPermission>()
	}
}