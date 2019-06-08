package com.mrpowergamerbr.loritta.website.views.subviews.configure

import com.mrpowergamerbr.loritta.website.evaluate
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import net.dv8tion.jda.api.entities.Guild
import org.jooby.Request
import org.jooby.Response

class ConfigureAutoroleView : ConfigureView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		super.handleRender(req, res, path, variables)
		return path.matches(Regex("^/dashboard/configure/[0-9]+/autorole"))
	}

	override fun renderConfiguration(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth, guild: Guild, serverConfig: MongoServerConfig): String {
		variables["saveType"] = "autorole"
		serverConfig.autoroleConfig.roles = serverConfig.autoroleConfig.roles.filter {
			try {
				guild.getRoleById(it) != null
			} catch (e: Exception) {
				false
			}
		}.toMutableList()
		variables["currentAutoroles"] = serverConfig.autoroleConfig.roles.joinToString(separator = ";")
		return evaluate("autorole.html", variables)
	}
}