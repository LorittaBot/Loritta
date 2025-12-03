package net.perfectdreams.loritta.morenitta.websitedashboard.routes.banappeals

import io.ktor.server.application.*
import io.ktor.server.request.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute

abstract class RequiresUserAuthBanAppealsLocalizedRoute(website: LorittaDashboardWebServer, originalPath: String) : RequiresUserAuthDashboardLocalizedRoute(website, originalPath) {
    override suspend fun onUnauthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext) {
        if (call.request.userAgent() == Constants.DISCORD_CRAWLER_USER_AGENT) {
            respondWithDiscordLoginPage(
                call,
                i18nContext.get(I18nKeysData.Website.BanAppeals.Title),
                "Quebrou as regras da Loritta, se arrependeu e quer uma segunda chance? Então envie um apelo de ban!",
                "https://stuff.loritta.website/loritta-and-wumpus-dashboard-yafyr.png"
            )
            return
        }

        respondWithDiscordAuthRedirect(call)
    }
}