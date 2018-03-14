package com.mrpowergamerbr.loritta.frontend.views.subviews.configure

import com.github.salomonbrys.kotson.set
import com.google.gson.JsonArray
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.frontend.evaluate
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import net.dv8tion.jda.core.entities.Guild
import org.jooby.Request
import org.jooby.Response

class ConfigureAminoView : ConfigureView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		super.handleRender(req, res, path, variables)
		return path.matches(Regex("^/dashboard/configure/[0-9]+/amino"))
	}

	override fun renderConfiguration(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth, guild: Guild, serverConfig: ServerConfig): String {
		variables["saveType"] = "amino"
		serverConfig.aminoConfig.aminos = serverConfig.aminoConfig.aminos.filter { guild.getTextChannelById(it.repostToChannelId) != null }.toMutableList()

		return evaluate("amino.html", variables)
	}
}