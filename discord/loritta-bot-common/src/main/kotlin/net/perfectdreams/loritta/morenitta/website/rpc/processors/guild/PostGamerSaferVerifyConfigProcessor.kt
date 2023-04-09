package net.perfectdreams.loritta.morenitta.website.rpc.processors.guild

import io.ktor.server.application.*
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferRequiresVerificationRoles
import net.perfectdreams.loritta.morenitta.utils.GuildLorittaUser
import net.perfectdreams.loritta.morenitta.utils.LorittaPermission
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.rpc.processors.LorittaRpcProcessor
import net.perfectdreams.loritta.serializable.requests.GetGamerSaferVerifyConfigRequest
import net.perfectdreams.loritta.serializable.requests.PostGamerSaferVerifyConfigRequest
import net.perfectdreams.loritta.serializable.responses.DiscordAccountError
import net.perfectdreams.loritta.serializable.responses.GetGamerSaferVerifyConfigResponse
import net.perfectdreams.loritta.serializable.responses.LorittaRPCResponse
import net.perfectdreams.loritta.serializable.responses.PostGamerSaferVerifyConfigResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import kotlin.time.Duration

class PostGamerSaferVerifyConfigProcessor(val m: LorittaWebsite) : LorittaRpcProcessor {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(call: ApplicationCall, request: PostGamerSaferVerifyConfigRequest): PostGamerSaferVerifyConfigResponse {
        val guildId = request.guildId
        val serverConfig = m.loritta.getOrCreateServerConfig(guildId) // get server config for guild

        when (val result = getDiscordAccountInformation(m.loritta, call)) {
            LorittaRpcProcessor.DiscordAccountInformationResult.InvalidDiscordAuthorization -> return DiscordAccountError.InvalidDiscordAuthorization()
            LorittaRpcProcessor.DiscordAccountInformationResult.UserIsLorittaBanned -> return DiscordAccountError.UserIsLorittaBanned()
            is LorittaRpcProcessor.DiscordAccountInformationResult.Success -> {
                val jdaGuild = m.loritta.lorittaShards.getGuildById(guildId)
                    ?: return PostGamerSaferVerifyConfigResponse.UnknownGuild()

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
                    return PostGamerSaferVerifyConfigResponse.Unauthorized()

                // TODO: Create GamerSafer Guild config if needed
                // TODO: Check what roles the users have and remove them to require verification

                m.loritta.transaction {
                    // Delete all verification roles
                    GamerSaferRequiresVerificationRoles.deleteWhere {
                        GamerSaferRequiresVerificationRoles.guild eq jdaGuild.idLong
                    }

                    // Reinsert them!
                    for (role in request.verificationRoles) {
                        GamerSaferRequiresVerificationRoles.insert {
                            it[GamerSaferRequiresVerificationRoles.guild] = jdaGuild.idLong
                            it[GamerSaferRequiresVerificationRoles.role] = role.roleId
                            it[GamerSaferRequiresVerificationRoles.checkPeriod] = Duration.parseIsoString(role.time).inWholeMilliseconds
                        }
                    }
                }

                return PostGamerSaferVerifyConfigResponse.Success()
            }
        }
    }
}