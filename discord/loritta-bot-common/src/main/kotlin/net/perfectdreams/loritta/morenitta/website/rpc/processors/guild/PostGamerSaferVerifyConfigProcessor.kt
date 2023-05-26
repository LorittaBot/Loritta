package net.perfectdreams.loritta.morenitta.website.rpc.processors.guild

import dev.minn.jda.ktx.coroutines.await
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import io.ktor.server.application.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferGuilds
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferRequiresVerificationUsers
import net.perfectdreams.loritta.morenitta.tables.ServerConfigs
import net.perfectdreams.loritta.morenitta.utils.GuildLorittaUser
import net.perfectdreams.loritta.morenitta.utils.LorittaPermission
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.morenitta.utils.extensions.retrieveMemberOrNullById
import net.perfectdreams.loritta.morenitta.utils.gamersafer.GamerSaferUtils
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.rpc.processors.LorittaRpcProcessor
import net.perfectdreams.loritta.serializable.requests.PostGamerSaferVerifyConfigRequest
import net.perfectdreams.loritta.serializable.responses.DiscordAccountError
import net.perfectdreams.loritta.serializable.responses.PostGamerSaferVerifyConfigResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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

                val isGamerSaferGuildPresent = m.loritta.transaction {
                    GamerSaferGuilds.select {
                        GamerSaferGuilds.guildId eq guildId
                    }.count() != 0L
                }

                if (!isGamerSaferGuildPresent) {
                    val guildInfo = GamerSaferUtils.createGuildInfo(m.loritta, guildId)

                    val guildCreateResponse =
                        m.loritta.http.post("${m.loritta.config.loritta.gamerSafer.endpointUrl}/guilds") {
                            bearerAuth(guildInfo.jws)

                            setBody(
                                TextContent(
                                    buildJsonObject {
                                        put("provider", guildInfo.provider)
                                        put("providerId", guildInfo.providerId)
                                        put("name", jdaGuild.name)
                                    }.toString(),
                                    ContentType.Application.Json
                                )
                            )
                        }

                    if (guildCreateResponse.status == HttpStatusCode.Created) {
                        val creationPayload = guildCreateResponse.bodyAsText()
                        m.loritta.transaction {
                            GamerSaferGuilds.insert {
                                it[GamerSaferGuilds.guildId] = guildId
                                it[GamerSaferGuilds.creationPayload] = creationPayload
                            }
                        }
                    }
                }

                m.loritta.transaction {
                    val gsGuildConfigId = ServerConfigs.innerJoin(GamerSaferConfigs).select {
                        ServerConfigs.id eq guildId
                    }
                        .limit(1)
                        .firstOrNull()
                        ?.get(GamerSaferConfigs.id)

                    if (gsGuildConfigId != null) {
                        GamerSaferConfigs.update({ GamerSaferConfigs.id eq gsGuildConfigId }) {
                            it[GamerSaferConfigs.enabled] = request.enabled
                            it[GamerSaferConfigs.verifiedRoleId] = request.verifiedRoleId
                        }
                    } else {
                        val newGsGuildConfigId = GamerSaferConfigs.insertAndGetId {
                            it[GamerSaferConfigs.enabled] = request.enabled
                            it[GamerSaferConfigs.verifiedRoleId] = request.verifiedRoleId
                        }

                        ServerConfigs.update({ ServerConfigs.id eq guildId }) {
                            it[ServerConfigs.gamerSaferConfig] = newGsGuildConfigId
                        }
                    }

                    // Delete all verification users
                    GamerSaferRequiresVerificationUsers.deleteWhere {
                        GamerSaferRequiresVerificationUsers.guild eq jdaGuild.idLong
                    }

                    // Reinsert them!
                    for (userAndRole in request.verificationRoles) {
                        GamerSaferRequiresVerificationUsers.insert {
                            it[GamerSaferRequiresVerificationUsers.guild] = jdaGuild.idLong
                            it[GamerSaferRequiresVerificationUsers.user] = userAndRole.userId
                            it[GamerSaferRequiresVerificationUsers.role] = userAndRole.roleId
                            it[GamerSaferRequiresVerificationUsers.checkPeriod] = Duration.parseIsoString(userAndRole.time).inWholeMilliseconds
                        }
                    }
                }

                // Remove all roles that requires verification from the members
                for (userAndRole in request.verificationRoles) {
                    val role = jdaGuild.getRoleById(userAndRole.roleId) ?: continue
                    val member = jdaGuild.retrieveMemberOrNullById(userAndRole.userId) ?: continue
                    jdaGuild.removeRoleFromMember(member, role).await()
                }

                return PostGamerSaferVerifyConfigResponse.Success()
            }
        }
    }
}