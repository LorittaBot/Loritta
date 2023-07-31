package net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors.guild

import io.ktor.server.application.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferRequiresVerificationUsers
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.tables.ServerConfigs
import net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors.LorittaInternalRpcProcessor
import net.perfectdreams.loritta.serializable.GamerSaferVerificationUserAndRole
import net.perfectdreams.loritta.serializable.config.GuildGamerSaferConfig
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse
import org.jetbrains.exposed.sql.select
import kotlin.time.Duration.Companion.milliseconds

class GetGuildGamerSaferConfigProcessor(val m: LorittaBot) : LorittaInternalRpcProcessor<LorittaInternalRPCRequest.GetGuildGamerSaferConfigRequest, LorittaInternalRPCResponse.GetGuildGamerSaferConfigResponse> {
    override suspend fun process(
        call: ApplicationCall,
        request: LorittaInternalRPCRequest.GetGuildGamerSaferConfigRequest
    ): LorittaInternalRPCResponse.GetGuildGamerSaferConfigResponse {
        val guild = getGuildByIdValidateMemberManageGuildPermissionsOrFailWithResponse(
            m,
            request.guildId,
            request.memberIdToBePermissionCheckedAgainst,
            LorittaInternalRPCResponse.GetGuildGamerSaferConfigResponse.UnknownGuild(),
            LorittaInternalRPCResponse.GetGuildGamerSaferConfigResponse.UnknownMember(),
            LorittaInternalRPCResponse.GetGuildGamerSaferConfigResponse.MissingPermission(),
        )

        val (gsGuildConfig, verificationRoles) = m.transaction {
            val gsGuildConfig = ServerConfigs.innerJoin(GamerSaferConfigs).select {
                ServerConfigs.id eq guild.idLong
            }
                .limit(1)
                .firstOrNull()

            val verificationRoles = GamerSaferRequiresVerificationUsers.select {
                GamerSaferRequiresVerificationUsers.guild eq guild.idLong
            }.map {
                GamerSaferVerificationUserAndRole(
                    it[GamerSaferRequiresVerificationUsers.user],
                    null,
                    it[GamerSaferRequiresVerificationUsers.role],
                    it[GamerSaferRequiresVerificationUsers.checkPeriod].milliseconds.toIsoString()
                )
            }

            Pair(gsGuildConfig, verificationRoles)
        }

        return LorittaInternalRPCResponse.GetGuildGamerSaferConfigResponse.Success(
            GuildGamerSaferConfig(
                gsGuildConfig?.get(GamerSaferConfigs.verifiedRoleId),
                verificationRoles
            )
        )
    }
}