package net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors.guild

import io.ktor.server.application.*
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors.LorittaInternalRpcProcessor
import net.perfectdreams.loritta.serializable.DiscordGuild
import net.perfectdreams.loritta.serializable.DiscordRole
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse

class GetGuildInfoProcessor(val m: LorittaBot) : LorittaInternalRpcProcessor<LorittaInternalRPCRequest.GetGuildInfoRequest, LorittaInternalRPCResponse.GetGuildInfoResponse> {
    override suspend fun process(
        call: ApplicationCall,
        request: LorittaInternalRPCRequest.GetGuildInfoRequest
    ): LorittaInternalRPCResponse.GetGuildInfoResponse {
        val guild = getGuildByIdValidateMemberManageGuildPermissionsOrFailWithResponse(
            m,
            request.guildId,
            request.memberIdToBePermissionCheckedAgainst,
            LorittaInternalRPCResponse.GetGuildInfoResponse.UnknownGuild(),
            LorittaInternalRPCResponse.GetGuildInfoResponse.UnknownMember(),
            LorittaInternalRPCResponse.GetGuildInfoResponse.MissingPermission(),
        )

        return LorittaInternalRPCResponse.GetGuildInfoResponse.Success(
            DiscordGuild(
                guild.idLong,
                guild.name,
                guild.iconId,
                guild.roles.map {
                    DiscordRole(
                        it.idLong,
                        it.name,
                        it.colorRaw
                    )
                }
            )
        )
    }
}