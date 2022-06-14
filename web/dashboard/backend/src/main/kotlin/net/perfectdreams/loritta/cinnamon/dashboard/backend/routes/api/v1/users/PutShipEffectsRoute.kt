package net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.users

import io.ktor.http.*
import io.ktor.server.application.*
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.receiveAndDecodeRequest
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.respondLoritta
import net.perfectdreams.loritta.cinnamon.dashboard.common.LorittaJsonWebSession
import net.perfectdreams.loritta.cinnamon.dashboard.common.requests.PutShipEffectsRequest
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.PutShipEffectsResponse
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.ShipEffects
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import kotlin.time.Duration.Companion.days

class PutShipEffectsRoute(m: LorittaDashboardBackend) : RequiresAPIDiscordLoginRoute(m, "/api/v1/users/ship-effects") {
    override suspend fun onAuthenticatedRequest(
        call: ApplicationCall,
        userIdentification: LorittaJsonWebSession.UserIdentification
    ) {
        val request = call.receiveAndDecodeRequest<PutShipEffectsRequest>()

        m.pudding.transaction {
            val userProfile = m.pudding.users._getUserProfile(UserId(userIdentification.id.toLong()))
            if (userProfile == null || 3000 > userProfile.money) {
                // If the user profile is null or they don't have enough money, quit
                // TODO: Proper API response
                return@transaction
            }

            ShipEffects.insert {
                it[ShipEffects.buyerId] = userIdentification.id.toLong()
                it[ShipEffects.user1Id] = userIdentification.id.toLong()
                it[ShipEffects.user2Id] = request.receivingEffectUserId
                it[ShipEffects.editedShipValue] = request.percentage.percentage
                it[ShipEffects.expiresAt] = System.currentTimeMillis() + 7.days.inWholeMilliseconds
            }

            // Remove the sonhos
            Profiles.update({ Profiles.id eq userIdentification.id.toLong() }) {
                with(SqlExpressionBuilder) {
                    it.update(Profiles.money, Profiles.money - 3000)
                }
            }

            // TODO: Ship effects transaction type
        }

        call.respondLoritta(PutShipEffectsResponse, status = HttpStatusCode.Created)
    }
}