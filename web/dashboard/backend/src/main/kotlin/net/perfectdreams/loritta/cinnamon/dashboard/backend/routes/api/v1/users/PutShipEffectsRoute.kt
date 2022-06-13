package net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.users

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.receiveAndDecodeJson
import net.perfectdreams.loritta.cinnamon.dashboard.common.LorittaJsonWebSession
import net.perfectdreams.loritta.cinnamon.dashboard.common.requests.PutShipEffectsRequest
import net.perfectdreams.loritta.cinnamon.pudding.tables.ShipEffects
import org.jetbrains.exposed.sql.insert
import kotlin.time.Duration.Companion.days

class PutShipEffectsRoute(m: LorittaDashboardBackend) : RequiresAPIDiscordLoginRoute(m, "/api/v1/users/ship-effects") {
    override suspend fun onAuthenticatedRequest(
        call: ApplicationCall,
        userIdentification: LorittaJsonWebSession.UserIdentification
    ) {
        if (true) {
            // Disabled for now
            call.respondText(
                "",
                status = HttpStatusCode.Forbidden
            )
            return
        }

        val request = call.receiveAndDecodeJson<PutShipEffectsRequest>()

        m.pudding.transaction {
            ShipEffects.insert {
                // TODO: Get ID from authenticated request
                it[ShipEffects.buyerId] = userIdentification.id.toLong()
                it[ShipEffects.user1Id] = userIdentification.id.toLong()
                it[ShipEffects.user2Id] = request.receivingEffectUserId
                it[ShipEffects.editedShipValue] = request.percentage.percentage
                it[ShipEffects.expiresAt] = System.currentTimeMillis() + 7.days.inWholeMilliseconds
            }
        }

        call.respondText("", status = HttpStatusCode.Created)
    }
}