package net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.economy

import io.ktor.server.application.*
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.LorittaDashboardRpcProcessor
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.serializable.StoredPowerStreamClaimedFirstSonhosRewardSonhosTransaction
import net.perfectdreams.loritta.serializable.dashboard.requests.LorittaDashboardRPCRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.update
import java.time.Instant

class PutPowerStreamClaimedFirstSonhosRewardProcessor(val m: LorittaDashboardBackend) : LorittaDashboardRpcProcessor<LorittaDashboardRPCRequest.PutPowerStreamClaimedFirstSonhosRewardRequest, LorittaDashboardRPCResponse.PutPowerStreamClaimedFirstSonhosRewardResponse> {
    override suspend fun process(call: ApplicationCall, request: LorittaDashboardRPCRequest.PutPowerStreamClaimedFirstSonhosRewardRequest): LorittaDashboardRPCResponse.PutPowerStreamClaimedFirstSonhosRewardResponse {
        when (validateDashboardToken(m, call)) {
            LorittaDashboardRpcProcessor.DashboardTokenResult.InvalidTokenAuthorization -> {
                return LorittaDashboardRPCResponse.PutPowerStreamClaimedFirstSonhosRewardResponse.Unauthorized()
            }
            LorittaDashboardRpcProcessor.DashboardTokenResult.Success -> {
                val result = m.pudding.transaction {
                    val changedProfilesCount = Profiles.update({ Profiles.id eq request.userId }) {
                        with(SqlExpressionBuilder) {
                            it[money] = money + request.quantity
                        }
                    }

                    if (changedProfilesCount == 0)
                        return@transaction false

                    // Cinnamon transaction log
                    SimpleSonhosTransactionsLogUtils.insert(
                        request.userId,
                        Instant.now(),
                        TransactionType.POWERSTREAM,
                        request.quantity,
                        StoredPowerStreamClaimedFirstSonhosRewardSonhosTransaction(request.liveId, request.streamId)
                    )

                    return@transaction true
                }

                return when (result) {
                    true -> LorittaDashboardRPCResponse.PutPowerStreamClaimedFirstSonhosRewardResponse.Success()
                    false -> LorittaDashboardRPCResponse.PutPowerStreamClaimedFirstSonhosRewardResponse.UnknownUser()
                }
            }
        }
    }
}