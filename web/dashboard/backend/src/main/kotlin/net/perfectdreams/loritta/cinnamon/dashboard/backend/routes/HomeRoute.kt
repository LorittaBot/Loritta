package net.perfectdreams.loritta.cinnamon.dashboard.backend.routes

import io.ktor.server.application.*
import io.ktor.server.html.*
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.common.LorittaJsonWebSession

class HomeRoute(m: LorittaDashboardBackend) : RequiresDiscordLoginRoute(m, "/") {
    override suspend fun onAuthenticatedRequest(
        call: ApplicationCall,
        userIdentification: LorittaJsonWebSession.UserIdentification
    ) {
        call.respondHtml(
            block = galleryOfDreamsSpaHtml(m, "SpicyMorenitta") {}
        )
    }
}