package com.mrpowergamerbr.loritta.website.views.subviews.configure

import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.website.evaluate
import net.dv8tion.jda.api.entities.Guild
import org.jooby.Request
import org.jooby.Response

class ConfigureCommandsView : ConfigureView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		super.handleRender(req, res, path, variables)
		return path.matches(Regex("^/dashboard/configure/[0-9]+/commands"))
	}

	override fun renderConfiguration(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth, guild: Guild, serverConfig: MongoServerConfig): String {
		variables["saveType"] = "vanilla_commands"

		variables["enabledCommands"] = loritta.commandManager.commands.filter { !serverConfig.disabledCommands.contains(it.javaClass.simpleName) }
		variables["enabledLegacyCommands"] = loritta.legacyCommandManager.commandMap.filter { !serverConfig.disabledCommands.contains(it.javaClass.simpleName) }
		variables["disabledCommands"] = loritta.commandManager.commands.filter { serverConfig.disabledCommands.contains(it.javaClass.simpleName) }
		variables["disabledLegacyCommands"] = loritta.legacyCommandManager.commandMap.filter { serverConfig.disabledCommands.contains(it.javaClass.simpleName) }

		return evaluate("configure_commands.html", variables)
	}
}