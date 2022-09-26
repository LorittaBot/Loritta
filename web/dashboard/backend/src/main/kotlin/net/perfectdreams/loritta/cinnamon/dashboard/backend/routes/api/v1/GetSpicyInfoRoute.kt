package net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1

import io.ktor.server.application.*
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.respondLoritta
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetSpicyInfoResponse
import net.perfectdreams.sequins.ktor.BaseRoute

class GetSpicyInfoRoute(private val m: LorittaDashboardBackend) : BaseRoute("/api/v1/spicy") {
    override suspend fun onRequest(call: ApplicationCall) {
        call.respondLoritta(
            GetSpicyInfoResponse(
                "Howdy! Did you know that Loritta is open source? https://github.com/LorittaBot/Loritta :3",
                m.config.legacyDashboardUrl.removeSuffix("/")
            )
        )
    }
}