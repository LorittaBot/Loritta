package net.perfectdreams.loritta.morenitta.website.routes

import io.ktor.server.application.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserWebsiteSettings
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

abstract class RequiresDiscordLoginLocalizedDashboardRoute(loritta: LorittaBot, path: String) : RequiresDiscordLoginLocalizedRoute(loritta, path) {
    override suspend fun onAuthenticatedRequest(
        call: ApplicationCall,
        locale: BaseLocale,
        i18nContext: I18nContext,
        discordAuth: TemmieDiscordAuth,
        userIdentification: LorittaJsonWebSession.UserIdentification
    ) {
        val dashboardColorThemePreference = loritta.transaction {
            UserWebsiteSettings.select(UserWebsiteSettings.dashboardColorThemePreference)
                .where { UserWebsiteSettings.id eq userIdentification.id.toLong() }
                .firstOrNull()
                ?.get(UserWebsiteSettings.dashboardColorThemePreference)
        } ?: ColorTheme.LIGHT

        onDashboardAuthenticatedRequest(
            call,
            locale,
            i18nContext,
            discordAuth,
            userIdentification,
            dashboardColorThemePreference
        )
    }

    abstract suspend fun onDashboardAuthenticatedRequest(
        call: ApplicationCall,
        locale: BaseLocale,
        i18nContext: I18nContext,
        discordAuth: TemmieDiscordAuth,
        userIdentification: LorittaJsonWebSession.UserIdentification,
        colorTheme: ColorTheme
    )
}