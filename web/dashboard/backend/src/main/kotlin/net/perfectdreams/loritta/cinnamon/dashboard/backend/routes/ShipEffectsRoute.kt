package net.perfectdreams.loritta.cinnamon.dashboard.backend.routes

import io.ktor.server.application.*
import io.ktor.server.html.*
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.LorittaJsonWebSession
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.TemmieDiscordAuth
import net.perfectdreams.loritta.cinnamon.dashboard.common.RoutePaths

class ShipEffectsRoute(m: LorittaDashboardBackend) : RequiresDiscordLoginRoute(m, RoutePaths.SHIP_EFFECTS) {
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