package net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.guild

import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.DiscordUtils
import net.perfectdreams.loritta.serializable.dashboard.requests.LorittaDashboardRPCRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse

class GetGuildGamerSaferConfigProcessor(m: LorittaDashboardBackend) : GuildRPCHotwireProcessor<
        LorittaDashboardRPCRequest.GetGuildGamerSaferConfigRequest,
        LorittaDashboardRPCResponse.GetGuildGamerSaferConfigResponse,

        LorittaInternalRPCRequest.GetGuildGamerSaferConfigRequest,
        LorittaInternalRPCResponse.GetGuildGamerSaferConfigResponse> (
    m,
    { request, identification ->
        val cluster = DiscordUtils.getLorittaClusterForGuildId(m, request.guildId)

        Pair(
            cluster,
            LorittaInternalRPCRequest.GetGuildGamerSaferConfigRequest(
                request.guildId,
                identification.id.toLong(),
            )
        )
    },
    {
        if (it !is LorittaInternalRPCResponse.GetGuildGamerSaferConfigResponse.Success)
            error("Unexpected response")

        LorittaDashboardRPCResponse.GetGuildGamerSaferConfigResponse.Success(it.config)
    },
    { LorittaDashboardRPCResponse.GetGuildGamerSaferConfigResponse.MissingPermission() },
    { LorittaDashboardRPCResponse.GetGuildGamerSaferConfigResponse.UnknownGuild() },
    { LorittaDashboardRPCResponse.GetGuildGamerSaferConfigResponse.UnknownMember() }
)