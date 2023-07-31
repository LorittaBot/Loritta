package net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors.guild

import dev.minn.jda.ktx.coroutines.await
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferGuilds
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferRequiresVerificationUsers
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.tables.ServerConfigs
import net.perfectdreams.loritta.morenitta.utils.extensions.retrieveMemberOrNullById
import net.perfectdreams.loritta.morenitta.utils.gamersafer.GamerSaferUtils
import net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors.LorittaInternalRpcProcessor
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlin.time.Duration

class UpdateGuildGamerSaferConfigProcessor(val m: LorittaBot) : LorittaInternalRpcProcessor<LorittaInternalRPCRequest.UpdateGuildGamerSaferConfigRequest, LorittaInternalRPCResponse.UpdateGuildGamerSaferConfigResponse> {
    override suspend fun process(
        call: ApplicationCall,
        request: LorittaInternalRPCRequest.UpdateGuildGamerSaferConfigRequest
    ): LorittaInternalRPCResponse.UpdateGuildGamerSaferConfigResponse {
        val guild = getGuildByIdValidateMemberManageGuildPermissionsOrFailWithResponse(
            m,
            request.guildId,
            request.memberIdToBePermissionCheckedAgainst,
            LorittaInternalRPCResponse.UpdateGuildGamerSaferConfigResponse.UnknownGuild(),
            LorittaInternalRPCResponse.UpdateGuildGamerSaferConfigResponse.UnknownMember(),
            LorittaInternalRPCResponse.UpdateGuildGamerSaferConfigResponse.MissingPermission(),
        )

        val isGamerSaferGuildPresent = m.transaction {
            GamerSaferGuilds.select {
                GamerSaferGuilds.guildId eq guild.idLong
            }.count() != 0L
        }

        if (!isGamerSaferGuildPresent) {
            val guildInfo = GamerSaferUtils.createGuildInfo(m, guild.idLong)

            val guildCreateResponse = m.http.post("${m.config.loritta.gamerSafer.endpointUrl}/guilds") {
                bearerAuth(guildInfo.jws)

                setBody(
                    TextContent(
                        buildJsonObject {
                            put("provider", guildInfo.provider)
                            put("providerId", guildInfo.providerId)
                            put("name", guild.name)
                        }.toString(),
                        ContentType.Application.Json
                    )
                )
            }

            if (guildCreateResponse.status == HttpStatusCode.Created) {
                val creationPayload = guildCreateResponse.bodyAsText()
                m.transaction {
                    GamerSaferGuilds.insert {
                        it[GamerSaferGuilds.guildId] = guildId
                        it[GamerSaferGuilds.creationPayload] = creationPayload
                    }
                }
            }
        }

        m.transaction {
            val gsGuildConfigId = ServerConfigs.innerJoin(GamerSaferConfigs).select {
                ServerConfigs.id eq guild.idLong
            }
                .limit(1)
                .firstOrNull()
                ?.get(GamerSaferConfigs.id)

            if (gsGuildConfigId != null) {
                GamerSaferConfigs.update({ GamerSaferConfigs.id eq gsGuildConfigId }) {
                    it[GamerSaferConfigs.enabled] = true
                    it[GamerSaferConfigs.verifiedRoleId] = request.config.verificationRoleId
                }
            } else {
                val newGsGuildConfigId = GamerSaferConfigs.insertAndGetId {
                    it[GamerSaferConfigs.enabled] = true
                    it[GamerSaferConfigs.verifiedRoleId] = request.config.verificationRoleId
                }

                ServerConfigs.update({ ServerConfigs.id eq guild.idLong }) {
                    it[ServerConfigs.gamerSaferConfig] = newGsGuildConfigId
                }
            }

            // Delete all verification users
            GamerSaferRequiresVerificationUsers.deleteWhere {
                GamerSaferRequiresVerificationUsers.guild eq guild.idLong
            }

            // Reinsert them!
            for (userAndRole in request.config.verificationRoles) {
                GamerSaferRequiresVerificationUsers.insert {
                    it[GamerSaferRequiresVerificationUsers.guild] = guild.idLong
                    it[GamerSaferRequiresVerificationUsers.user] = userAndRole.userId
                    it[GamerSaferRequiresVerificationUsers.role] = userAndRole.roleId
                    it[GamerSaferRequiresVerificationUsers.checkPeriod] =
                        Duration.parseIsoString(userAndRole.time).inWholeMilliseconds
                }
            }
        }

        // Remove all roles that requires verification from the members
        for (userAndRole in request.config.verificationRoles) {
            val role = guild.getRoleById(userAndRole.roleId) ?: continue
            val member = guild.retrieveMemberOrNullById(userAndRole.userId) ?: continue
            guild.removeRoleFromMember(member, role).await()
        }

        return LorittaInternalRPCResponse.UpdateGuildGamerSaferConfigResponse.Success()
    }
}