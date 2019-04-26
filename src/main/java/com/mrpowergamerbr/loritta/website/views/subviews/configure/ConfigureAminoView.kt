package com.mrpowergamerbr.loritta.website.views.subviews.configure

import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.extensions.getTextChannelByNullableId
import com.mrpowergamerbr.loritta.website.evaluate
import net.dv8tion.jda.api.entities.Guild
import org.jooby.Request
import org.jooby.Response

class ConfigureAminoView : ConfigureView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		super.handleRender(req, res, path, variables)
		return path.matches(Regex("^/dashboard/configure/[0-9]+/amino"))
	}

	override fun renderConfiguration(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth, guild: Guild, serverConfig: MongoServerConfig): String {
		variables["saveType"] = "amino"
		serverConfig.aminoConfig.aminos = serverConfig.aminoConfig.aminos.filter { guild.getTextChannelByNullableId(it.repostToChannelId) != null }.toMutableList()

		return evaluate("amino.html", variables)
	}
}