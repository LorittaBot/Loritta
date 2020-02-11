package com.mrpowergamerbr.loritta.website.views.subviews.configure

import com.mrpowergamerbr.loritta.website.evaluate
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import net.dv8tion.jda.api.entities.Guild
import org.jooby.Request
import org.jooby.Response

class ConfigureInviteBlockerView : ConfigureView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		super.handleRender(req, res, path, variables)
		return path.matches(Regex("^/dashboard/configure/[0-9]+/inviteblocker"))
	}

	override fun renderConfiguration(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth, guild: Guild, serverConfig: MongoServerConfig): String {
		variables["saveType"] = "invite_blocker"
		variables["whitelistedChannels"] = serverConfig.inviteBlockerConfig.whitelistedChannels.filter{ guild.getTextChannelById(it) != null }.joinToString(separator = ";")
		return evaluate("invite_blocker.html", variables)
	}
}