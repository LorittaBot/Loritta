package com.mrpowergamerbr.loritta.frontend.views.subviews

import com.mrpowergamerbr.loritta.frontend.evaluate
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import net.dv8tion.jda.core.entities.Guild
import org.jooby.Request
import org.jooby.Response

class ConfigureInviteBlockerView : ConfigureView() {
	override fun handleRender(req: Request, res: Response, variables: MutableMap<String, Any?>): Boolean {
		super.handleRender(req, res, variables)
		return req.path().matches(Regex("^/dashboard/configure/[0-9]+/inviteblocker"))
	}

	override fun renderConfiguration(req: Request, res: Response, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth, guild: Guild, serverConfig: ServerConfig): String {
		variables["saveType"] = "invite_blocker"
		variables["whitelistedChannels"] = serverConfig.inviteBlockerConfig.whitelistedChannels.filter{ guild.getTextChannelById(it) != null }.joinToString(separator = ";")
		return evaluate("invite_blocker.html", variables)
	}
}