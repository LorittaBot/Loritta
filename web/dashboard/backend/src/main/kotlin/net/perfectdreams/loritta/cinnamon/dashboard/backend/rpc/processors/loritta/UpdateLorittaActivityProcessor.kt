package net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.loritta

import io.ktor.server.application.*
import kotlinx.datetime.toJavaInstant
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.LorittaDashboardRpcProcessor
import net.perfectdreams.loritta.cinnamon.pudding.tables.GatewayActivities
import net.perfectdreams.loritta.serializable.dashboard.requests.LorittaDashboardRPCRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse
import org.jetbrains.exposed.sql.insert
import java.time.Instant

class UpdateLorittaActivityProcessor(val m: LorittaDashboardBackend) : LorittaDashboardRpcProcessor<LorittaDashboardRPCRequest.UpdateLorittaActivityRequest, LorittaDashboardRPCResponse.UpdateLorittaActivityResponse> {
    override suspend fun process(call: ApplicationCall, request: LorittaDashboardRPCRequest.UpdateLorittaActivityRequest): LorittaDashboardRPCResponse.UpdateLorittaActivityResponse {
        when (validateDashboardToken(m, call)) {
            LorittaDashboardRpcProcessor.DashboardTokenResult.InvalidTokenAuthorization -> {
                return LorittaDashboardRPCResponse.UpdateLorittaActivityResponse.Unauthorized()
            }
            LorittaDashboardRpcProcessor.DashboardTokenResult.Success -> {
                m.pudding.transaction {
                    GatewayActivities.insert {
                        it[GatewayActivities.text] = request.text
                        it[GatewayActivities.type] = request.type
                        it[GatewayActivities.priority] = request.priority
                        it[GatewayActivities.streamUrl] = request.streamUrl
                        it[GatewayActivities.submittedAt] = Instant.now()
                        it[GatewayActivities.startsAt] = request.startsAt.toJavaInstant()
                        it[GatewayActivities.endsAt] = request.endsAt.toJavaInstant()
                    }
                }

                return LorittaDashboardRPCResponse.UpdateLorittaActivityResponse.Success()
            }
        }
    }
}