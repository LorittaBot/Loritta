package net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors

import io.ktor.server.application.*
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.DiscordUtils
import net.perfectdreams.loritta.serializable.dashboard.requests.LorittaDashboardRPCRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.DashGuildScopedResponse
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse

class ExecuteDashGuildScopedProcessor(val m: LorittaDashboardBackend) : LorittaDashboardRpcProcessor<LorittaDashboardRPCRequest.ExecuteDashGuildScopedRPCRequest, LorittaDashboardRPCResponse.ExecuteDashGuildScopedRPCResponse> {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun process(
        call: ApplicationCall,
        request: LorittaDashboardRPCRequest.ExecuteDashGuildScopedRPCRequest
    ): LorittaDashboardRPCResponse.ExecuteDashGuildScopedRPCResponse {
        when (val accountInformationResult = getDiscordAccountInformation(m, call)) {
            LorittaDashboardRpcProcessor.DiscordAccountInformationResult.InvalidDiscordAuthorization -> return LorittaDashboardRPCResponse.ExecuteDashGuildScopedRPCResponse(DashGuildScopedResponse.InvalidDiscordAuthorization)
            LorittaDashboardRpcProcessor.DiscordAccountInformationResult.UserIsLorittaBanned -> TODO()
            is LorittaDashboardRpcProcessor.DiscordAccountInformationResult.Success -> {
                val cluster = DiscordUtils.getLorittaClusterForGuildId(m, request.guildId)

                val response = m.makeRPCRequest<LorittaInternalRPCResponse>(
                    cluster,
                    LorittaInternalRPCRequest.ExecuteDashGuildScopedRPCRequest(
                        request.guildId,
                        accountInformationResult.userIdentification.id.toLong(),
                        request.request
                    )
                )

                if (response is LorittaInternalRPCResponse.ExecuteDashGuildScopedRPCResponse) {
                    return LorittaDashboardRPCResponse.ExecuteDashGuildScopedRPCResponse(response.response)
                } else error("Whoops! ${response::class.simpleName}")
            }
        }
    }
}