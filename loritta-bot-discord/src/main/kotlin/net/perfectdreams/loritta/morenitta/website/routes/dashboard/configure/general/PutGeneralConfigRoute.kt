package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.general

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedDashboardRoute
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.config.GuildGeneralConfigBootstrap
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class PutGeneralConfigRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure") {
	override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {
		val config = Json.decodeFromString<GuildGeneralConfigBootstrap.GuildGeneralConfig>(call.receiveText())

		loritta.newSuspendedTransaction {
			val serverConfig = loritta.getOrCreateServerConfig(guild.idLong)

			serverConfig.commandPrefix = config.prefix
			serverConfig.deleteMessageAfterCommand = config.deleteMessageAfterCommand
			serverConfig.warnOnUnknownCommand = config.warnOnUnknownCommand
			serverConfig.warnIfBlacklisted = config.warnIfBlacklisted
			serverConfig.blacklistedChannels = config.blacklistedChannels.take(DiscordResourceLimits.Guild.Channels)
			serverConfig.blacklistedWarning = config.blacklistedWarning
		}

		call.respond(HttpStatusCode.NoContent)
	}
}