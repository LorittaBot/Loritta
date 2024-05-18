package net.perfectdreams.loritta.morenitta.website.routes.dashboard

import io.ktor.server.application.*
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserWebsiteSettings
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

abstract class RequiresGuildAuthLocalizedDashboardRoute(loritta: LorittaBot, originalDashboardPath: String) : RequiresGuildAuthLocalizedRoute(loritta, originalDashboardPath) {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onGuildAuthenticatedRequest(
		call: ApplicationCall,
		locale: BaseLocale,
		i18nContext: I18nContext,
		discordAuth: TemmieDiscordAuth,
		userIdentification: LorittaJsonWebSession.UserIdentification,
		guild: Guild,
		serverConfig: ServerConfig
	) {
		val dashboardColorThemePreference = loritta.transaction {
			UserWebsiteSettings.select(UserWebsiteSettings.dashboardColorThemePreference)
				.where { UserWebsiteSettings.id eq userIdentification.id.toLong() }
				.firstOrNull()
				?.get(UserWebsiteSettings.dashboardColorThemePreference)
		} ?: ColorTheme.LIGHT

		return onDashboardGuildAuthenticatedRequest(call, locale, i18nContext, discordAuth, userIdentification, guild, serverConfig, dashboardColorThemePreference)
	}

	abstract suspend fun onDashboardGuildAuthenticatedRequest(
		call: ApplicationCall,
		locale: BaseLocale,
		i18nContext: I18nContext,
		discordAuth: TemmieDiscordAuth,
		userIdentification: LorittaJsonWebSession.UserIdentification,
		guild: Guild,
		serverConfig: ServerConfig,
		colorTheme: ColorTheme
	)
}