package net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors

import io.ktor.server.application.*
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.extensions.retrieveMemberOrNullById
import net.perfectdreams.loritta.morenitta.websiteinternal.rpc.RPCResponseException
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse

interface LorittaInternalRpcProcessor<Req: LorittaInternalRPCRequest, Res: LorittaInternalRPCResponse> {
    suspend fun process(call: ApplicationCall, request: Req): Res

    suspend fun getGuildByIdValidateMemberManageGuildPermissionsOrFailWithResponse(
        m: LorittaBot,
        guildId: Long,
        memberId: Long?,
        unknownGuildResponse: Res,
        unknownMemberResponse: Res,
        missingPermissionResponse: Res
    ): Guild {
        val guild = getGuildByIdOrFailWithResponse(m, guildId, unknownGuildResponse)

        if (memberId == null)
            return guild

        val member = guild.retrieveMemberOrNullById(memberId) ?: throw RPCResponseException(unknownMemberResponse)

        val hasPermissionToAccessTheDashboard = member.hasPermission(Permission.MANAGE_SERVER) || member.hasPermission(Permission.ADMINISTRATOR) || member.isOwner

        if (!hasPermissionToAccessTheDashboard)
            throw RPCResponseException(missingPermissionResponse)

        return guild
    }

    suspend fun getGuildByIdOrFailWithResponse(m: LorittaBot, guildId: Long, unknownGuildResponse: Res): Guild {
        return m.lorittaShards.getGuildById(guildId) ?: throw RPCResponseException(unknownGuildResponse)
    }
}