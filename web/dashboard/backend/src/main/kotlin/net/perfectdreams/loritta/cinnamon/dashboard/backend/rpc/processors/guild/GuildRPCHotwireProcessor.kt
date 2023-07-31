package net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.guild

import io.ktor.server.application.*
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.LorittaDashboardRpcProcessor
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.LorittaJsonWebSession
import net.perfectdreams.loritta.serializable.dashboard.requests.LorittaDashboardRPCRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse

/**
 * A quick and dirty way to hotwire Dashboard <-> RPC connections, without the boilerplate
 */
open class GuildRPCHotwireProcessor<
        Req: LorittaDashboardRPCRequest,
        Res: LorittaDashboardRPCResponse,

        InternalReq: LorittaInternalRPCRequest,
        InternalRes: LorittaInternalRPCResponse>(
    val m: LorittaDashboardBackend,
    private val dashboardRequestToRPCRequestRemapper: (Req, LorittaJsonWebSession.UserIdentification) -> (Pair<LorittaInternalRPCResponse.GetLorittaReplicasInfoResponse.LorittaCluster, InternalReq>),
    private val rpcResponseToDashboardResponseRemapper: (InternalRes) -> Res,
    private val missingPermissionResponse: () -> Res,
    private val unknownGuildResponse: () -> Res,
    private val unknownMemberResponse: () -> Res
) : LorittaDashboardRpcProcessor<Req, Res> {
    override suspend fun process(call: ApplicationCall, request: Req): Res {
        when (val accountInformationResult = getDiscordAccountInformation(m, call)) {
            LorittaDashboardRpcProcessor.DiscordAccountInformationResult.InvalidDiscordAuthorization -> TODO()
            LorittaDashboardRpcProcessor.DiscordAccountInformationResult.UserIsLorittaBanned -> TODO()
            is LorittaDashboardRpcProcessor.DiscordAccountInformationResult.Success -> {
                val (cluster, rpcRequest) = dashboardRequestToRPCRequestRemapper.invoke(request, accountInformationResult.userIdentification)

                val response = m.makeRPCRequest<LorittaInternalRPCResponse>(
                    cluster,
                    rpcRequest
                ) as InternalRes // We can't use InternalRes as the <> type because it must be reified

                return when (response) {
                    is LorittaInternalRPCResponse.MissingPermissionError -> missingPermissionResponse.invoke()
                    is LorittaInternalRPCResponse.UnknownGuildError -> unknownGuildResponse.invoke()
                    is LorittaInternalRPCResponse.UnknownMemberError -> unknownMemberResponse.invoke()
                    else -> {
                        rpcResponseToDashboardResponseRemapper.invoke(response)
                    }
                }
            }
        }
    }
}