package net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors

import com.github.benmanes.caffeine.cache.Caffeine
import io.ktor.server.application.*
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.concrete.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.CustomGuildCommands
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.StarboardConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.WelcomerConfigs
import net.perfectdreams.loritta.common.utils.placeholders.JoinMessagePlaceholders
import net.perfectdreams.loritta.common.utils.placeholders.LeaveMessagePlaceholders
import net.perfectdreams.loritta.common.utils.placeholders.MessagePlaceholder
import net.perfectdreams.loritta.common.utils.placeholders.SectionPlaceholders
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.StarboardConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.WelcomerConfig
import net.perfectdreams.loritta.morenitta.modules.WelcomeModule
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.utils.extensions.retrieveMemberOrNullById
import net.perfectdreams.loritta.morenitta.websiteinternal.InternalWebServer
import net.perfectdreams.loritta.morenitta.websiteinternal.rpc.RPCResponseException
import net.perfectdreams.loritta.serializable.*
import net.perfectdreams.loritta.serializable.config.GuildCustomCommand
import net.perfectdreams.loritta.serializable.config.GuildCustomCommandsConfig
import net.perfectdreams.loritta.serializable.config.GuildStarboardConfig
import net.perfectdreams.loritta.serializable.config.GuildWelcomerConfig
import net.perfectdreams.loritta.serializable.dashboard.requests.DashGuildScopedRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.DashGuildScopedResponse
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.concurrent.TimeUnit
import kotlin.collections.set

class ExecuteDashGuildScopedProcessor(private val internalWebServer: InternalWebServer, val m: LorittaBot) : LorittaInternalRpcProcessor<LorittaInternalRPCRequest.ExecuteDashGuildScopedRPCRequest, LorittaInternalRPCResponse.ExecuteDashGuildScopedRPCResponse> {
    // THE HACKIEST HACK YOU EVER HACKED
    val messageSendCooldown = Caffeine.newBuilder().expireAfterAccess(30L, TimeUnit.SECONDS).maximumSize(100).build<Long, Long>().asMap()

    // TODO: Allow overriding what types of requests require what kinds of permissions
    override suspend fun process(
        call: ApplicationCall,
        request: LorittaInternalRPCRequest.ExecuteDashGuildScopedRPCRequest
    ): LorittaInternalRPCResponse.ExecuteDashGuildScopedRPCResponse {
        val dashRequest = request.dashRequest

        val guild = m.lorittaShards.getGuildById(request.guildId) ?: throw RPCResponseException(LorittaInternalRPCResponse.ExecuteDashGuildScopedRPCResponse(DashGuildScopedResponse.UnknownGuild))

        val member = guild.retrieveMemberOrNullById(request.memberIdToBePermissionCheckedAgainst) ?: throw RPCResponseException(LorittaInternalRPCResponse.ExecuteDashGuildScopedRPCResponse(DashGuildScopedResponse.UnknownMember))

        val hasPermissionToAccessTheDashboard = member.hasPermission(Permission.MANAGE_SERVER) || member.hasPermission(Permission.ADMINISTRATOR) || member.isOwner

        if (!hasPermissionToAccessTheDashboard)
            throw RPCResponseException(LorittaInternalRPCResponse.ExecuteDashGuildScopedRPCResponse(DashGuildScopedResponse.MissingPermission))

        val user = member.user

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
                when (it) {
                    is TextChannel -> {
                        TextDiscordChannel(
                            it.idLong,
                            it.name,
                            it.canTalk()
                        )
                    }
                    is VoiceChannel -> {
                        VoiceDiscordChannel(
                            it.idLong,
                            it.name
                        )
                    }

                    is Category -> {
                        CategoryDiscordChannel(
                            it.idLong,
                            it.name
                        )
                    }

                    is NewsChannel -> {
                        NewsDiscordChannel(
                            it.idLong,
                            it.name,
                            it.canTalk()
                        )
                    }

                    is StageChannel -> {
                        StageDiscordChannel(
                            it.idLong,
                            it.name
                        )
                    }

                    is ForumChannel -> {
                        ForumDiscordChannel(
                            it.idLong,
                            it.name
                        )
                    }

                    else -> UnknownDiscordChannel(
                        it.idLong,
                        it.name
                    )
                }
            }
        )

        val serializableSelfLorittaUser = DiscordUser(
            guild.selfMember.user.idLong,
            guild.selfMember.user.name,
            guild.selfMember.user.globalName,
            guild.selfMember.user.discriminator,
            guild.selfMember.user.avatarId
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
                    serializableSelfLorittaUser,
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

            is DashGuildScopedRequest.GetGuildStarboardConfigRequest -> {
                val result = m.transaction {
                    ServerConfigs.innerJoin(StarboardConfigs).select {
                        ServerConfigs.id eq guild.idLong
                    }.firstOrNull()
                }

                DashGuildScopedResponse.GetGuildStarboardConfigResponse(
                    serializableGuild,
                    serializableSelfLorittaUser,
                    result?.let {
                        GuildStarboardConfig(
                            it[StarboardConfigs.enabled],
                            it[StarboardConfigs.starboardChannelId],
                            it[StarboardConfigs.requiredStars]
                        )
                    }
                )
            }

            is DashGuildScopedRequest.GetGuildCustomCommandsConfigRequest -> {
                val customCommands = m.newSuspendedTransaction {
                    CustomGuildCommands.select {
                        CustomGuildCommands.guild eq guild.idLong
                    }.map {
                        GuildCustomCommand(
                            it[CustomGuildCommands.id].value,
                            it[CustomGuildCommands.label],
                            it[CustomGuildCommands.codeType],
                            it[CustomGuildCommands.code]
                        )
                    }
                }

                DashGuildScopedResponse.GetGuildCustomCommandsConfigResponse(
                    serializableGuild,
                    serializableSelfLorittaUser,
                    GuildCustomCommandsConfig(customCommands)
                )
            }

            is DashGuildScopedRequest.SendMessageRequest -> run {
                // Rate Limit
                val last = messageSendCooldown.getOrDefault(user.idLong, 0L)

                val diff = System.currentTimeMillis() - last
                if (4000 >= diff)
                    return@run DashGuildScopedResponse.SendMessageResponse.TooManyMessages

                messageSendCooldown[user.idLong] = System.currentTimeMillis()

                val channelId = dashRequest.channelId
                val channel = if (channelId == null) {
                    user.openPrivateChannel().await()
                } else {
                    guild.getGuildMessageChannelById(channelId) ?: return@run DashGuildScopedResponse.SendMessageResponse.UnknownChannel
                }

                // This is a WORKAROUND (as always...)
                // We are going to "map down" the placeholders into the customTokens map
                val section = SectionPlaceholders.sections.first { it.type == dashRequest.placeholderSectionType }

                // Just to skip all the cruft
                fun <T : MessagePlaceholder> generateMessage(section: SectionPlaceholders<T>, builder: (T) -> (String)) = MessageUtils.generateMessage(
                    dashRequest.message,
                    guild,
                    section,
                    builder
                )

                val message = when (section) {
                    is JoinMessagePlaceholders -> generateMessage(section, WelcomeModule.buildJoinMessagePlaceholders(guild, user))
                    is LeaveMessagePlaceholders -> generateMessage(section, WelcomeModule.buildLeaveMessagePlaceholders(guild, user))
                }!!

                try {
                    channel.sendMessage(message).await()
                } catch (e: Exception) {
                    return@run DashGuildScopedResponse.SendMessageResponse.FailedToSendMessage
                }

                DashGuildScopedResponse.SendMessageResponse.Success
            }

            is DashGuildScopedRequest.UpdateGuildWelcomerConfigRequest -> {
                m.newSuspendedTransaction {
                    val serverConfig = m.getOrCreateServerConfig(guild.idLong)

                    val welcomerConfig = serverConfig.welcomerConfig

                    val newConfig = welcomerConfig ?: WelcomerConfig.new {
                        this.tellOnJoin = dashRequest.config.tellOnJoin
                        this.channelJoinId = dashRequest.config.channelJoinId
                        this.joinMessage = dashRequest.config.joinMessage
                        this.deleteJoinMessagesAfter = dashRequest.config.deleteJoinMessagesAfter
                        this.tellOnRemove = dashRequest.config.tellOnRemove
                        this.channelRemoveId = dashRequest.config.channelRemoveId
                        this.removeMessage = dashRequest.config.removeMessage
                        this.deleteRemoveMessagesAfter = dashRequest.config.deleteRemoveMessagesAfter
                        this.tellOnBan = dashRequest.config.tellOnBan
                        this.bannedMessage = dashRequest.config.bannedMessage
                        this.tellOnPrivateJoin = dashRequest.config.tellOnPrivateJoin
                        this.joinPrivateMessage = dashRequest.config.joinPrivateMessage
                    }

                    newConfig.tellOnJoin = dashRequest.config.tellOnJoin
                    newConfig.channelJoinId = dashRequest.config.channelJoinId
                    newConfig.joinMessage = dashRequest.config.joinMessage
                    newConfig.deleteJoinMessagesAfter = dashRequest.config.deleteJoinMessagesAfter
                    newConfig.tellOnRemove = dashRequest.config.tellOnRemove
                    newConfig.channelRemoveId = dashRequest.config.channelRemoveId
                    newConfig.removeMessage = dashRequest.config.removeMessage
                    newConfig.deleteRemoveMessagesAfter = dashRequest.config.deleteRemoveMessagesAfter
                    newConfig.tellOnBan = dashRequest.config.tellOnBan
                    newConfig.bannedMessage = dashRequest.config.bannedMessage
                    newConfig.tellOnPrivateJoin = dashRequest.config.tellOnPrivateJoin
                    newConfig.joinPrivateMessage = dashRequest.config.joinPrivateMessage

                    serverConfig.welcomerConfig = newConfig
                }
                DashGuildScopedResponse.UpdateGuildWelcomerConfigResponse
            }

            is DashGuildScopedRequest.UpdateGuildStarboardConfigRequest -> {
                m.newSuspendedTransaction {
                    val serverConfig = m.getOrCreateServerConfig(guild.idLong)

                    val starboardConfig = serverConfig.starboardConfig

                    if (!dashRequest.config.enabled) {
                        serverConfig.starboardConfig = null
                        starboardConfig?.delete()
                    } else {
                        val newConfig = starboardConfig ?: StarboardConfig.new {
                            this.enabled = false
                            this.starboardChannelId = -1
                            this.requiredStars = 1
                        }

                        newConfig.enabled = dashRequest.config.enabled
                        newConfig.starboardChannelId = dashRequest.config.starboardChannelId ?: -1
                        newConfig.requiredStars = dashRequest.config.requiredStars

                        serverConfig.starboardConfig = newConfig
                    }
                }
                DashGuildScopedResponse.UpdateGuildStarboardConfigResponse
            }

            is DashGuildScopedRequest.GetGuildCustomCommandConfigRequest -> {
                val customCommand = m.newSuspendedTransaction {
                    CustomGuildCommands.select {
                        CustomGuildCommands.guild eq guild.idLong and (CustomGuildCommands.id eq dashRequest.commandId)
                    }.limit(1).first().let {
                        GuildCustomCommand(
                            it[CustomGuildCommands.id].value,
                            it[CustomGuildCommands.label],
                            it[CustomGuildCommands.codeType],
                            it[CustomGuildCommands.code]
                        )
                    }
                }

                DashGuildScopedResponse.GetGuildCustomCommandConfigResponse(
                    serializableGuild,
                    serializableSelfLorittaUser,
                    customCommand
                )
            }

            is DashGuildScopedRequest.UpsertGuildCustomCommandConfigRequest -> {
                m.newSuspendedTransaction {
                    val updateCount = CustomGuildCommands.update({
                        CustomGuildCommands.id eq dashRequest.id and (CustomGuildCommands.guild eq guild.idLong)
                    }) {
                        it[label] = dashRequest.label
                        it[codeType] = dashRequest.codeType
                        it[code] = dashRequest.code
                    }

                    if (updateCount == 0) {
                        // Does not exist, so let's insert it!
                        val commandId = CustomGuildCommands.insertAndGetId {
                            it[enabled] = true
                            it[CustomGuildCommands.guild] = guild.idLong
                            it[label] = dashRequest.label
                            it[codeType] = dashRequest.codeType
                            it[code] = dashRequest.code
                        }
                        return@newSuspendedTransaction DashGuildScopedResponse.UpsertGuildCustomCommandConfigResponse(commandId.value)
                    }

                    // The update count should NEVER be == 0 with a null ID
                    return@newSuspendedTransaction DashGuildScopedResponse.UpsertGuildCustomCommandConfigResponse(dashRequest.id!!)
                }

            }

            is DashGuildScopedRequest.DeleteGuildCustomCommandConfigRequest -> {
                m.newSuspendedTransaction {
                    CustomGuildCommands.deleteWhere {
                        CustomGuildCommands.id eq dashRequest.commandId and (CustomGuildCommands.guild eq guild.idLong)
                    }
                }
                DashGuildScopedResponse.DeleteGuildCustomCommandConfigResponse
            }
        }

        return LorittaInternalRPCResponse.ExecuteDashGuildScopedRPCResponse(dashResponse)
    }
}