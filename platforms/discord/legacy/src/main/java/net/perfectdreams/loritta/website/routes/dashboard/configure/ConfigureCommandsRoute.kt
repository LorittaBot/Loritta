package net.perfectdreams.loritta.website.routes.dashboard.configure

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.ServerConfig
import net.perfectdreams.loritta.common.locale.BaseLocale
import com.mrpowergamerbr.loritta.website.evaluate
import io.ktor.application.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.dashboard.RequiresGuildAuthLocalizedRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class ConfigureCommandsRoute(loritta: LorittaDiscord) : RequiresGuildAuthLocalizedRoute(loritta, "/configure/commands") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		loritta as Loritta
		val variables = call.legacyVariables(locale)
		variables["saveType"] = "vanilla_commands"

		variables["enabledLegacyCommands"] = com.mrpowergamerbr.loritta.utils.loritta.legacyCommandManager.commandMap.filter { !serverConfig.disabledCommands.contains(it.javaClass.simpleName) }
		variables["enabledNewCommands"] = com.mrpowergamerbr.loritta.utils.loritta.commandMap.commands.filter { !serverConfig.disabledCommands.contains(it.commandName) }
				.map {
					NewCommandWrapper(
							it.commandName,
							it.labels.toTypedArray()
					)
				}

		variables["disabledLegacyCommands"] = com.mrpowergamerbr.loritta.utils.loritta.legacyCommandManager.commandMap.filter { serverConfig.disabledCommands.contains(it.javaClass.simpleName) }
		variables["disabledNewCommands"] = com.mrpowergamerbr.loritta.utils.loritta.commandMap.commands.filter { serverConfig.disabledCommands.contains(it.commandName) }
				.map {
					NewCommandWrapper(
							it.commandName,
							it.labels.toTypedArray()
					)
				}

		call.respondHtml(evaluate("configure_commands.html", variables))
	}

	/**
	 * Command Wrapper for the new commands that do use the DSL syntax
	 *
	 * We create a wrapper because the frontend still uses Pebble, and Pebble ain't that smart to figure out static methods (sadly), this
	 * will be removed when this route frontend is migrated to Kotlin/JS
	 */
	class NewCommandWrapper(
			val commandName: String,
			val labels: Array<String>
	)
}