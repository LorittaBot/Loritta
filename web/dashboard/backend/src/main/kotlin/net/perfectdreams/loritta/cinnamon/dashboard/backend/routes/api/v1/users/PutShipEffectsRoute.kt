package net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.users

import io.ktor.http.*
import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.receiveAndDecodeRequest
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.respondLoritta
import net.perfectdreams.loritta.cinnamon.dashboard.common.requests.PutShipEffectsRequest
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.NotEnoughSonhosErrorResponse
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.PutShipEffectsResponse
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.ShipEffects
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.serializable.StoredShipEffectSonhosTransaction
import net.perfectdreams.loritta.serializable.UserId
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import kotlin.time.Duration.Companion.days

class PutShipEffectsRoute(m: LorittaDashboardBackend) : RequiresAPIDiscordLoginRoute(m, "/api/v1/users/ship-effects") {
    companion object {
        private const val SHIP_EFFECT_COST = 3_000L
    }

    override suspend fun onAuthenticatedRequest(
        call: ApplicationCall,
        discordAuth: TemmieDiscordAuth,
        userIdentification: LorittaJsonWebSession.UserIdentification
    ) {
        val selfUserId = userIdentification.id.toLong()
        val request = call.receiveAndDecodeRequest<PutShipEffectsRequest>()

        m.pudding.transaction {
            val userProfile = m.pudding.users.getUserProfile(UserId(userIdentification.id.toLong()))
            if (userProfile == null || SHIP_EFFECT_COST > userProfile.money) {
                // If the user profile is null, or they don't have enough money, quit
                // TODO: Move it outside of here
                runBlocking {
                    call.respondLoritta(NotEnoughSonhosErrorResponse, status = HttpStatusCode.Forbidden)
                }
                return@transaction
            }

            val now = Clock.System.now()

            val shipEffectId = ShipEffects.insertAndGetId {
                it[ShipEffects.buyerId] = selfUserId
                it[ShipEffects.user1Id] = selfUserId
                it[ShipEffects.user2Id] = request.receivingEffectUserId
                it[ShipEffects.editedShipValue] = request.percentage.percentage
                it[ShipEffects.expiresAt] = (now + 7.days).toEpochMilliseconds()
            }

            // Cinnamon transaction log
            SimpleSonhosTransactionsLogUtils.insert(
                selfUserId,
                now.toJavaInstant(),
                TransactionType.SHIP_EFFECT,
                SHIP_EFFECT_COST,
                StoredShipEffectSonhosTransaction(shipEffectId.value)
            )

            // Remove the sonhos
            Profiles.update({ Profiles.id eq userIdentification.id.toLong() }) {
                with(SqlExpressionBuilder) {
                    it.update(Profiles.money, Profiles.money - SHIP_EFFECT_COST)
                }
            }
        }

        call.respondLoritta(PutShipEffectsResponse, status = HttpStatusCode.Created)
    }
}