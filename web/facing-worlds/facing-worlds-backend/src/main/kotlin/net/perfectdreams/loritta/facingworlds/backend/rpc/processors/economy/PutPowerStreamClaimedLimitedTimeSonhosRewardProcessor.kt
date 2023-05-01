package net.perfectdreams.loritta.facingworlds.backend.rpc.processors.economy

import io.ktor.http.*
import io.ktor.server.application.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.PowerStreamClaimedLimitedTimeSonhosRewardSonhosTransactionsLog
import net.perfectdreams.loritta.facingworlds.backend.FacingWorldsBackend
import net.perfectdreams.loritta.facingworlds.backend.rpc.processors.FacingWorldsRpcProcessor
import net.perfectdreams.loritta.facingworlds.common.v1.PutPowerStreamClaimedLimitedTimeSonhosRewardRequest
import net.perfectdreams.loritta.facingworlds.common.v1.PutPowerStreamClaimedLimitedTimeSonhosRewardResponse
import org.jetbrains.exposed.sql.*
import java.time.Instant

class PutPowerStreamClaimedLimitedTimeSonhosRewardProcessor(val m: FacingWorldsBackend) : FacingWorldsRpcProcessor {
    suspend fun process(call: ApplicationCall, request: PutPowerStreamClaimedLimitedTimeSonhosRewardRequest): FacingWorldsRpcProcessor.ProcessorResponse {
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

            PowerStreamClaimedLimitedTimeSonhosRewardSonhosTransactionsLog.insert {
                it[timestampLog] = transactionLogId
                it[sonhos] = request.quantity
                it[streamId] = request.streamId
                it[rewardId] = request.rewardId
            }

            return@transaction true
        }

        return when (result) {
            true -> FacingWorldsRpcProcessor.ProcessorResponse(
                HttpStatusCode.OK,
                PutPowerStreamClaimedLimitedTimeSonhosRewardResponse.Success()
            )
            false -> FacingWorldsRpcProcessor.ProcessorResponse(
                HttpStatusCode.NotFound,
                PutPowerStreamClaimedLimitedTimeSonhosRewardResponse.UnknownUser()
            )
        }
    }
}