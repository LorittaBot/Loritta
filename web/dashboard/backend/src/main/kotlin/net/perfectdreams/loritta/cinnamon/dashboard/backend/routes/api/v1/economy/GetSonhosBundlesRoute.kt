package net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.economy

import io.ktor.server.application.*
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.respondLoritta
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetSonhosBundlesResponse
import net.perfectdreams.loritta.cinnamon.pudding.data.SonhosBundle
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosBundles
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.select

class GetSonhosBundlesRoute(val m: LorittaDashboardBackend) : BaseRoute("/api/v1/economy/bundles/sonhos") {
    override suspend fun onRequest(call: ApplicationCall) {
        val sonhosBundles = m.pudding.transaction {
            SonhosBundles.select { SonhosBundles.active eq true }
                .toList()
        }

        call.respondLoritta(
            GetSonhosBundlesResponse(
                sonhosBundles.map {
                    SonhosBundle(
                        it[SonhosBundles.id].value,
                        it[SonhosBundles.active],
                        it[SonhosBundles.price],
                        it[SonhosBundles.sonhos]
                    )
                }
            )
        )
    }
}