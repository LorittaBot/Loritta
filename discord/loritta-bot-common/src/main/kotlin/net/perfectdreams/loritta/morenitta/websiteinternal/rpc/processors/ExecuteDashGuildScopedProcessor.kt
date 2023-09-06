package net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors

import io.ktor.server.application.*
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.WelcomerConfigs
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.extensions.retrieveMemberOrNullById
import net.perfectdreams.loritta.morenitta.websiteinternal.InternalWebServer
import net.perfectdreams.loritta.morenitta.websiteinternal.rpc.RPCResponseException
import net.perfectdreams.loritta.serializable.*
import net.perfectdreams.loritta.serializable.config.GuildWelcomerConfig
import net.perfectdreams.loritta.serializable.dashboard.requests.DashGuildScopedRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.DashGuildScopedResponse
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse
import org.jetbrains.exposed.sql.select

class ExecuteDashGuildScopedProcessor(private val internalWebServer: InternalWebServer, val m: LorittaBot) : LorittaInternalRpcProcessor<LorittaInternalRPCRequest.ExecuteDashGuildScopedRPCRequest, LorittaInternalRPCResponse.ExecuteDashGuildScopedRPCResponse> {
    // TODO: Allow overriding what types of requests require what kinds of permissions
    override suspend fun process(
        call: ApplicationCall,
        request: LorittaInternalRPCRequest.ExecuteDashGuildScopedRPCRequest
    ): LorittaInternalRPCResponse.ExecuteDashGuildScopedRPCResponse {
        val dashRequest = request.dashRequest

        val guild = m.lorittaShards.getGuildById(request.guildId) ?: throw RPCResponseException(LorittaInternalRPCResponse.ExecuteDashGuildScopedRPCResponse(
            DashGuildScopedResponse.UnknownGuild))

        val member = guild.retrieveMemberOrNullById(request.memberIdToBePermissionCheckedAgainst) ?: throw RPCResponseException(LorittaInternalRPCResponse.ExecuteDashGuildScopedRPCResponse(
            DashGuildScopedResponse.UnknownMember))

        val hasPermissionToAccessTheDashboard = member.hasPermission(Permission.MANAGE_SERVER) || member.hasPermission(Permission.ADMINISTRATOR) || member.isOwner

        if (!hasPermissionToAccessTheDashboard)
            throw RPCResponseException(LorittaInternalRPCResponse.ExecuteDashGuildScopedRPCResponse(DashGuildScopedResponse.MissingPermission))

        val serializableGuild = DiscordGuild(
            guild.idLong,
            guild.name,
            guild.iconId,
            guild.roles.map {
                DiscordRole(
                    it.idLong,
                    it.name,
                    it.colorRaw
                )
            },
            guild.channels.map {
                when (it.type) {
                    ChannelType.TEXT -> TextDiscordChannel(
                        it.idLong,
                        it.name
                    )
                    ChannelType.PRIVATE -> TODO()
                    ChannelType.VOICE -> VoiceDiscordChannel(
                        it.idLong,
                        it.name
                    )
                    ChannelType.GROUP -> TODO()
                    ChannelType.CATEGORY -> CategoryDiscordChannel(
                        it.idLong,
                        it.name
                    )
                    ChannelType.NEWS -> NewsDiscordChannel(
                        it.idLong,
                        it.name
                    )
                    ChannelType.STAGE -> StageDiscordChannel(
                        it.idLong,
                        it.name
                    )
                    ChannelType.GUILD_NEWS_THREAD -> TODO()
                    ChannelType.GUILD_PUBLIC_THREAD -> TODO()
                    ChannelType.GUILD_PRIVATE_THREAD -> TODO()
                    ChannelType.FORUM -> ForumDiscordChannel(
                        it.idLong,
                        it.name
                    )
                    else -> UnknownDiscordChannel(
                        it.idLong,
                        it.name
                    )
                }
            }
        )

        // Permissions has been checked, go through!
        val dashResponse = when (dashRequest) {
            is DashGuildScopedRequest.GetGuildInfoRequest -> {
                DashGuildScopedResponse.GetGuildInfoResponse(serializableGuild)
            }

            is DashGuildScopedRequest.GetGuildWelcomerConfigRequest -> {
                val result = m.transaction {
                    ServerConfigs.innerJoin(WelcomerConfigs).select {
                        ServerConfigs.id eq guild.idLong
                    }.firstOrNull()
                }

                DashGuildScopedResponse.GetGuildWelcomerConfigResponse(
                    serializableGuild,
                    DiscordUser(
                        guild.selfMember.user.idLong,
                        guild.selfMember.user.name,
                        guild.selfMember.user.globalName,
                        guild.selfMember.user.discriminator,
                        guild.selfMember.user.avatarId
                    ),
                    result?.let {
                        GuildWelcomerConfig(
                            it[WelcomerConfigs.tellOnJoin],
                            it[WelcomerConfigs.channelJoinId],
                            it[WelcomerConfigs.joinMessage],
                            it[WelcomerConfigs.deleteJoinMessagesAfter],

                            it[WelcomerConfigs.tellOnRemove],
                            it[WelcomerConfigs.channelRemoveId],
                            it[WelcomerConfigs.removeMessage],
                            it[WelcomerConfigs.deleteRemoveMessagesAfter],

                            it[WelcomerConfigs.tellOnPrivateJoin],
                            it[WelcomerConfigs.joinPrivateMessage],

                            it[WelcomerConfigs.tellOnBan],
                            it[WelcomerConfigs.bannedMessage],
                        )
                    }
                )
            }
        }

        return LorittaInternalRPCResponse.ExecuteDashGuildScopedRPCResponse(dashResponse)
    }
}