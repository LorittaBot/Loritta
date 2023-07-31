package net.perfectdreams.loritta.morenitta.website.rpc.processors.guild

import io.ktor.server.application.*
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferRequiresVerificationUsers
import net.perfectdreams.loritta.morenitta.tables.ServerConfigs
import net.perfectdreams.loritta.morenitta.utils.GuildLorittaUser
import net.perfectdreams.loritta.morenitta.utils.LorittaPermission
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.rpc.processors.LorittaRpcProcessor
import net.perfectdreams.loritta.serializable.GamerSaferVerificationUserAndRole
import net.perfectdreams.loritta.serializable.requests.GetGamerSaferVerifyConfigRequest
import net.perfectdreams.loritta.serializable.responses.DiscordAccountError
import net.perfectdreams.loritta.serializable.responses.GetGamerSaferVerifyConfigResponse
import net.perfectdreams.loritta.serializable.responses.LorittaRPCResponse
import org.jetbrains.exposed.sql.select
import kotlin.time.Duration.Companion.milliseconds

class GetGamerSaferVerifyConfigProcessor(val m: LorittaWebsite) : LorittaRpcProcessor {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(call: ApplicationCall, request: GetGamerSaferVerifyConfigRequest): LorittaRPCResponse {
        val guildId = request.guildId
        val serverConfig = m.loritta.getOrCreateServerConfig(guildId) // get server config for guild

        when (val result = getDiscordAccountInformation(m.loritta, call)) {
            LorittaRpcProcessor.DiscordAccountInformationResult.InvalidDiscordAuthorization -> return DiscordAccountError.InvalidDiscordAuthorization()
            LorittaRpcProcessor.DiscordAccountInformationResult.UserIsLorittaBanned -> return DiscordAccountError.UserIsLorittaBanned()
            is LorittaRpcProcessor.DiscordAccountInformationResult.Success -> {
                val jdaGuild = m.loritta.lorittaShards.getGuildById(guildId)
                    ?: return GetGamerSaferVerifyConfigResponse.UnknownGuild()

                val userIdentification = result.userIdentification
                val id = userIdentification.id
                val member = jdaGuild.retrieveMemberById(id).await()
                var canAccessDashboardViaPermission = false

                if (member != null) {
                    val lorittaUser = GuildLorittaUser(m.loritta, member, LorittaUser.loadMemberLorittaPermissions(m.loritta, serverConfig, member), m.loritta.getOrCreateLorittaProfile(id.toLong()))

                    canAccessDashboardViaPermission = lorittaUser.hasPermission(LorittaPermission.ALLOW_ACCESS_TO_DASHBOARD)
                }

                val canBypass = m.loritta.isOwner(userIdentification.id) || canAccessDashboardViaPermission
                if (!canBypass && !(member?.hasPermission(Permission.ADMINISTRATOR) == true || member?.hasPermission(Permission.MANAGE_SERVER) == true || jdaGuild.ownerId == userIdentification.id))
                    return GetGamerSaferVerifyConfigResponse.Unauthorized()

                val (gsGuildConfig, verificationRoles) = m.loritta.transaction {
                    val gsGuildConfig = ServerConfigs.innerJoin(GamerSaferConfigs).select {
                        ServerConfigs.id eq guildId
                    }
                        .limit(1)
                        .firstOrNull()

                    val verificationRoles = GamerSaferRequiresVerificationUsers.select {
                        GamerSaferRequiresVerificationUsers.guild eq jdaGuild.idLong
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

                return GetGamerSaferVerifyConfigResponse.Success(
                    gsGuildConfig?.get(GamerSaferConfigs.enabled) ?: false,
                    jdaGuild.roles.filter { !it.isManaged && !it.isPublicRole }.map {
                        GetGamerSaferVerifyConfigResponse.Role(
                            it.name,
                            it.idLong,
                            it.colorRaw
                        )
                    },
                    gsGuildConfig?.get(GamerSaferConfigs.verifiedRoleId),
                    verificationRoles
                )
            }
        }
    }
}