package net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.guild

import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.DiscordUtils
import net.perfectdreams.loritta.serializable.dashboard.requests.LorittaDashboardRPCRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse

class UpdateGuildGamerSaferConfigProcessor(m: LorittaDashboardBackend) : GuildRPCHotwireProcessor<
        LorittaDashboardRPCRequest.UpdateGuildGamerSaferConfigRequest,
        LorittaDashboardRPCResponse.UpdateGuildGamerSaferConfigResponse,

        LorittaInternalRPCRequest.UpdateGuildGamerSaferConfigRequest,
        LorittaInternalRPCResponse.UpdateGuildGamerSaferConfigResponse>(
    m,
    { request, identification ->
        val cluster = DiscordUtils.getLorittaClusterForGuildId(m, request.guildId)

        Pair(
            cluster,
            LorittaInternalRPCRequest.UpdateGuildGamerSaferConfigRequest(
                request.guildId,
                identification.id.toLong(),
                request.config
            )
        )
    },
    {
        if (it !is LorittaInternalRPCResponse.UpdateGuildGamerSaferConfigResponse.Success)
            error("Unexpected response")

        LorittaDashboardRPCResponse.UpdateGuildGamerSaferConfigResponse.Success()
    },
    { LorittaDashboardRPCResponse.UpdateGuildGamerSaferConfigResponse.MissingPermission() },
    { LorittaDashboardRPCResponse.UpdateGuildGamerSaferConfigResponse.UnknownGuild() },
    { LorittaDashboardRPCResponse.UpdateGuildGamerSaferConfigResponse.UnknownMember() }
)