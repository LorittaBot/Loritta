package com.mrpowergamerbr.loritta.frontend.views.subviews.configure

import com.mrpowergamerbr.loritta.frontend.evaluate
import com.mrpowergamerbr.loritta.userdata.PermissionsConfig
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Role
import org.jooby.Request
import org.jooby.Response
import kotlin.collections.set

class ConfigurePermissionsView : ConfigureView() {
	override fun handleRender(req: Request, res: Response, variables: MutableMap<String, Any?>): Boolean {
		super.handleRender(req, res, variables)
		return req.path().matches(Regex("^/dashboard/configure/[0-9]+/permissions"))
	}

	override fun renderConfiguration(req: Request, res: Response, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth, guild: Guild, serverConfig: ServerConfig): String {
		variables["saveType"] = "permissions"
		val roleConfig = mutableMapOf<Role, MutableMap<String, Boolean>>()

		for (role in guild.roles) {
			val roleConf = serverConfig.permissionsConfig.roles.getOrDefault(role.id, PermissionsConfig.PermissionRole())
			val permissionMap = mutableMapOf<String, Boolean>()

			for (permission in LorittaPermission.values()) {
				permissionMap[permission.internalName] = roleConf.permissions.contains(permission)
			}

			roleConfig[role] = permissionMap
		}

		variables["roleConfigs"] = roleConfig
		return evaluate("permissions.html", variables)
	}
}