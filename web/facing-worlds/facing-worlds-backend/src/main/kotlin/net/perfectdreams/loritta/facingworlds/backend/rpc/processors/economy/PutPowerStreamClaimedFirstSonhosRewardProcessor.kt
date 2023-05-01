package net.perfectdreams.loritta.facingworlds.backend.rpc.processors.economy

import io.ktor.http.*
import io.ktor.server.application.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.PowerStreamClaimedFirstSonhosRewardSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.PowerStreamClaimedLimitedTimeSonhosRewardSonhosTransactionsLog
import net.perfectdreams.loritta.facingworlds.backend.FacingWorldsBackend
import net.perfectdreams.loritta.facingworlds.backend.rpc.processors.FacingWorldsRpcProcessor
import net.perfectdreams.loritta.facingworlds.common.v1.PutPowerStreamClaimedFirstSonhosRewardRequest
import net.perfectdreams.loritta.facingworlds.common.v1.PutPowerStreamClaimedFirstSonhosRewardResponse
import net.perfectdreams.loritta.facingworlds.common.v1.PutPowerStreamClaimedLimitedTimeSonhosRewardRequest
import net.perfectdreams.loritta.facingworlds.common.v1.PutPowerStreamClaimedLimitedTimeSonhosRewardResponse
import org.jetbrains.exposed.sql.*
import java.time.Instant

class PutPowerStreamClaimedFirstSonhosRewardProcessor(val m: FacingWorldsBackend) : FacingWorldsRpcProcessor {
    suspend fun process(call: ApplicationCall, request: PutPowerStreamClaimedFirstSonhosRewardRequest): FacingWorldsRpcProcessor.ProcessorResponse {
        val result = m.pudding.transaction {
            val changedProfilesCount = Profiles.update({ Profiles.id eq request.userId }) {
                with(SqlExpressionBuilder) {
                    it[money] = money + request.quantity
                }
            }

            if (changedProfilesCount == 0)
                return@transaction false

            val transactionLogId = SonhosTransactionsLog.insertAndGetId {
                it[user] = request.userId
                it[timestamp] = Instant.now()
            }

            PowerStreamClaimedFirstSonhosRewardSonhosTransactionsLog.insert {
                it[timestampLog] = transactionLogId
                it[sonhos] = request.quantity
                it[streamId] = request.streamId
            }

            return@transaction true
        }

        return when (result) {
            true -> FacingWorldsRpcProcessor.ProcessorResponse(
                HttpStatusCode.OK,
                PutPowerStreamClaimedFirstSonhosRewardResponse.Success()
            )
            false -> FacingWorldsRpcProcessor.ProcessorResponse(
                HttpStatusCode.NotFound,
                PutPowerStreamClaimedFirstSonhosRewardResponse.UnknownUser()
            )
        }
    }
}