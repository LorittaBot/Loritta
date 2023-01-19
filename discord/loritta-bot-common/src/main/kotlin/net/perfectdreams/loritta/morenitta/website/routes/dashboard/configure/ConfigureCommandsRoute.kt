package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.website.evaluate
import io.ktor.server.application.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.LegacyPebbleGuildDashboardRawHtmlView
import net.perfectdreams.loritta.morenitta.website.views.LegacyPebbleRawHtmlView
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class ConfigureCommandsRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedRoute(loritta, "/configure/commands") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		loritta as LorittaBot
		val variables = call.legacyVariables(loritta, locale)
		variables["saveType"] = "vanilla_commands"

		variables["enabledLegacyCommands"] = loritta.legacyCommandManager.commandMap.filter { !serverConfig.disabledCommands.contains(it.javaClass.simpleName) }
		variables["enabledNewCommands"] = loritta.commandMap.commands.filter { !serverConfig.disabledCommands.contains(it.commandName) }
				.map {
					NewCommandWrapper(
							it.commandName,
							it.labels.toTypedArray()
					)
				}

		variables["disabledLegacyCommands"] = loritta.legacyCommandManager.commandMap.filter { serverConfig.disabledCommands.contains(it.javaClass.simpleName) }
		variables["disabledNewCommands"] = loritta.commandMap.commands.filter { serverConfig.disabledCommands.contains(it.commandName) }
				.map {
					NewCommandWrapper(
							it.commandName,
							it.labels.toTypedArray()
					)
				}

		call.respondHtml(
			LegacyPebbleGuildDashboardRawHtmlView(
				loritta,
				i18nContext,
				locale,
				getPathWithoutLocale(call),
				loritta.getLegacyLocaleById(locale.id),
				guild,
				"Painel de Controle",
				evaluate("configure_commands.html", variables),
				"vanilla_commands"
			).generateHtml()
		)
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