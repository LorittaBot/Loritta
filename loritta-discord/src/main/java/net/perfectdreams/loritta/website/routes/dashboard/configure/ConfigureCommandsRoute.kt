package net.perfectdreams.loritta.website.routes.dashboard.configure

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.website.evaluate
import io.ktor.application.ApplicationCall
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.dashboard.RequiresGuildAuthLocalizedRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class ConfigureCommandsRoute(loritta: LorittaDiscord) : RequiresGuildAuthLocalizedRoute(loritta, "/configure/commands") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild) {
		loritta as Loritta
		val variables = call.legacyVariables(locale)
		variables["saveType"] = "vanilla_commands"

		val serverConfig = loritta.getServerConfigForGuild(guild.id)

		variables["enabledCommands"] = com.mrpowergamerbr.loritta.utils.loritta.commandManager.commands.filter { !serverConfig.disabledCommands.contains(it.javaClass.simpleName) }
		variables["enabledLegacyCommands"] = com.mrpowergamerbr.loritta.utils.loritta.legacyCommandManager.commandMap.filter { !serverConfig.disabledCommands.contains(it.javaClass.simpleName) }
		variables["disabledCommands"] = com.mrpowergamerbr.loritta.utils.loritta.commandManager.commands.filter { serverConfig.disabledCommands.contains(it.javaClass.simpleName) }
		variables["disabledLegacyCommands"] = com.mrpowergamerbr.loritta.utils.loritta.legacyCommandManager.commandMap.filter { serverConfig.disabledCommands.contains(it.javaClass.simpleName) }

		call.respondHtml(evaluate("configure_commands.html", variables))
	}
}