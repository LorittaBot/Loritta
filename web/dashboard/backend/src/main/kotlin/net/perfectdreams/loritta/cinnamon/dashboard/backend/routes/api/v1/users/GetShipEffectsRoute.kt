package net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.users

import io.ktor.http.*
import io.ktor.server.application.*
import kotlinx.datetime.Instant
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.respondLoritta
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetShipEffectsResponse
import net.perfectdreams.loritta.cinnamon.pudding.tables.ShipEffects
import net.perfectdreams.loritta.serializable.ShipEffect
import net.perfectdreams.loritta.serializable.UserId
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.selectAll

class GetShipEffectsRoute(m: LorittaDashboardBackend) : RequiresAPIDiscordLoginRoute(m, "/api/v1/users/ship-effects") {
    override suspend fun onAuthenticatedRequest(
        call: ApplicationCall,
        discordAuth: TemmieDiscordAuth,
        userIdentification: LorittaJsonWebSession.UserIdentification
    ) {
        val shipEffects = m.pudding.transaction {
            ShipEffects.selectAll().where {
                ShipEffects.buyerId eq userIdentification.id.toLong()
            }.map { row ->
                ShipEffect(
                    row[ShipEffects.id].value,
                    UserId(row[ShipEffects.buyerId].toULong()),
                    UserId(row[ShipEffects.user1Id].toULong()),
                    UserId(row[ShipEffects.user2Id].toULong()),
                    row[ShipEffects.editedShipValue],
                    Instant.fromEpochMilliseconds(row[ShipEffects.expiresAt])
                )
            }
        }

        val resolvedUsers = shipEffects.flatMap { listOf(it.user1, it.user2, it.buyerId) }
            .distinct()
            .mapNotNull { m.pudding.users.getCachedUserInfoById(it) }

        call.respondLoritta(
            GetShipEffectsResponse(
                shipEffects,
                resolvedUsers
            ),
            status = HttpStatusCode.OK
        )
    }
}