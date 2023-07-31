package net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.guild

import io.ktor.server.application.*
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.LorittaDashboardRpcProcessor
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.DiscordUtils
import net.perfectdreams.loritta.serializable.dashboard.requests.LorittaDashboardRPCRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse

class GetGuildInfoProcessor(val m: LorittaDashboardBackend) : LorittaDashboardRpcProcessor<LorittaDashboardRPCRequest.GetGuildInfoRequest, LorittaDashboardRPCResponse.GetGuildInfoResponse> {
    override suspend fun process(
        call: ApplicationCall,
        request: LorittaDashboardRPCRequest.GetGuildInfoRequest
    ): LorittaDashboardRPCResponse.GetGuildInfoResponse {
        when (val accountInformationResult = getDiscordAccountInformation(m, call)) {
            LorittaDashboardRpcProcessor.DiscordAccountInformationResult.InvalidDiscordAuthorization -> TODO()
            LorittaDashboardRpcProcessor.DiscordAccountInformationResult.UserIsLorittaBanned -> TODO()
            is LorittaDashboardRpcProcessor.DiscordAccountInformationResult.Success -> {
                val cluster = DiscordUtils.getLorittaClusterForGuildId(m, request.guildId)

                val response = m.makeRPCRequest<LorittaInternalRPCResponse.GetGuildInfoResponse>(
                    cluster,
                    LorittaInternalRPCRequest.GetGuildInfoRequest(
                        request.guildId,
                        accountInformationResult.userIdentification.id.toLong()
                    )
                )

                return when (response) {
                    is LorittaInternalRPCResponse.GetGuildInfoResponse.Success -> {
                        LorittaDashboardRPCResponse.GetGuildInfoResponse.Success(response.guild)
                    }
                    is LorittaInternalRPCResponse.GetGuildInfoResponse.MissingPermission -> LorittaDashboardRPCResponse.GetGuildInfoResponse.MissingPermission()
                    is LorittaInternalRPCResponse.GetGuildInfoResponse.UnknownGuild -> LorittaDashboardRPCResponse.GetGuildInfoResponse.UnknownGuild()
                    is LorittaInternalRPCResponse.GetGuildInfoResponse.UnknownMember -> LorittaDashboardRPCResponse.GetGuildInfoResponse.UnknownMember()
                }
            }
        }
    }
}