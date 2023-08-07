package net.perfectdreams.loritta.cinnamon.dashboard.backend.routes

import io.ktor.server.application.*
import io.ktor.server.html.*
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.LorittaJsonWebSession
import net.perfectdreams.loritta.cinnamon.dashboard.common.RoutePaths
import net.perfectdreams.loritta.cinnamon.dashboard.common.buildToKtorPath
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class GuildsRoute(m: LorittaDashboardBackend) : RequiresDiscordLoginRoute(m, RoutePaths.GUILDS.buildToKtorPath()) {
    override suspend fun onAuthenticatedRequest(
        call: ApplicationCall,
        discordAuth: TemmieDiscordAuth,
        userIdentification: LorittaJsonWebSession.UserIdentification
    ) {
        call.respondHtml(
            block = galleryOfDreamsSpaHtml(m, "SpicyMorenitta") {}
        )
    }
}