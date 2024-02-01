package net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors

import com.github.benmanes.caffeine.cache.Caffeine
import io.ktor.http.*
import io.ktor.server.application.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.concrete.*
import net.perfectdreams.exposedpowerutils.sql.batchUpsert
import net.perfectdreams.loritta.cinnamon.pudding.tables.CachedTwitchChannels
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.CustomGuildCommands
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.*
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.placeholders.*
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.DonationKey
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
import net.perfectdreams.loritta.serializable.config.*
import net.perfectdreams.loritta.serializable.dashboard.requests.DashGuildScopedRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.DashGuildScopedResponse
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.collections.set
import kotlin.math.ceil

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

        val memberId = request.memberIdToBePermissionCheckedAgainst
        val cacheKey = "${request.guildId}#${memberId}"
        val isCachedMissingHit = m.cachedFailedMemberQueryResults.containsKey(cacheKey)
        if (isCachedMissingHit)
            throw RPCResponseException(LorittaInternalRPCResponse.ExecuteDashGuildScopedRPCResponse(DashGuildScopedResponse.UnknownMember))

        val member = guild.retrieveMemberOrNullById(request.memberIdToBePermissionCheckedAgainst)
        if (member == null) {
            m.cachedFailedMemberQueryResults[cacheKey] = true
            throw RPCResponseException(LorittaInternalRPCResponse.ExecuteDashGuildScopedRPCResponse(DashGuildScopedResponse.UnknownMember))
        }

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
            },
            guild.emojis.map {
                DiscordEmoji(
                    it.idLong,
                    it.name,
                    it.isAnimated
                )
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
                    is TwitchStreamOnlineMessagePlaceholders -> generateMessage(section) {
                        val additionalPlaceholdersInfo = dashRequest.additionalPlaceholdersInfo as DashGuildScopedRequest.SendMessageRequest.AdditionalPlaceholdersInfo.TwitchStreamOnlinePlaceholderInfo

                        when (it) {
                            TwitchStreamOnlineMessagePlaceholders.GuildIconUrlPlaceholder -> guild.iconUrl ?: ""
                            TwitchStreamOnlineMessagePlaceholders.GuildNamePlaceholder -> guild.name
                            TwitchStreamOnlineMessagePlaceholders.GuildSizePlaceholder -> guild.memberCount.toString()
                            TwitchStreamOnlineMessagePlaceholders.StreamGamePlaceholder -> "Just Chatting"
                            TwitchStreamOnlineMessagePlaceholders.StreamTitlePlaceholder -> "Configurando a Loritta!"
                            TwitchStreamOnlineMessagePlaceholders.StreamUrlPlaceholder -> "https://twitch.tv/${additionalPlaceholdersInfo.twitchLogin}"
                        }
                    }
                }

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

            is DashGuildScopedRequest.GetGuildTwitchConfigRequest -> {
                val (twitchAccounts, premiumTrackTwitchAccounts, valueOfTheDonationKeysEnabledOnThisGuild) = m.newSuspendedTransaction {
                    val twitchAccounts = TrackedTwitchAccounts.select { TrackedTwitchAccounts.guildId eq guild.idLong }
                        .map {
                            val state = getTwitchAccountTrackState(it[TrackedTwitchAccounts.twitchUserId])

                            Pair(
                                state,
                                TrackedTwitchAccount(
                                    it[TrackedTwitchAccounts.id].value,
                                    it[TrackedTwitchAccounts.twitchUserId],
                                    it[TrackedTwitchAccounts.channelId],
                                    it[TrackedTwitchAccounts.message]
                                )
                            )
                        }

                    val premiumTrackTwitchAccounts = PremiumTrackTwitchAccounts.select {
                        PremiumTrackTwitchAccounts.guildId eq guild.idLong
                    }.map {
                        PremiumTrackTwitchAccount(
                            it[PremiumTrackTwitchAccounts.id].value,
                            it[PremiumTrackTwitchAccounts.twitchUserId]
                        )
                    }

                    val valueOfTheDonationKeysEnabledOnThisGuild = DonationKey.find { DonationKeys.activeIn eq guild.idLong and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
                        .toList()
                        .sumOf { it.value }
                        .let { ceil(it) }

                    Triple(twitchAccounts, premiumTrackTwitchAccounts, valueOfTheDonationKeysEnabledOnThisGuild)
                }

                val accountsInfo = getCachedUsersInfoById(
                    *((twitchAccounts.map { it.second.twitchUserId } + premiumTrackTwitchAccounts.map { it.twitchUserId }).toSet()).toLongArray()
                )

                DashGuildScopedResponse.GetGuildTwitchConfigResponse(
                    serializableGuild,
                    serializableSelfLorittaUser,
                    valueOfTheDonationKeysEnabledOnThisGuild,
                    GuildTwitchConfig(
                        twitchAccounts.map { trackedTwitchAccount ->
                            GuildTwitchConfig.TrackedTwitchAccountWithTwitchUserAndTrackingState(
                                trackedTwitchAccount.first,
                                trackedTwitchAccount.second,
                                accountsInfo.firstOrNull { it.id == trackedTwitchAccount.second.twitchUserId }?.let {
                                    TwitchUser(it.id, it.login, it.displayName, it.profileImageUrl)
                                }
                            )
                        },
                        premiumTrackTwitchAccounts.map { trackedTwitchAccount ->
                            GuildTwitchConfig.PremiumTrackTwitchAccountWithTwitchUser(
                                trackedTwitchAccount,
                                accountsInfo.firstOrNull { it.id == trackedTwitchAccount.twitchUserId }?.let {
                                    TwitchUser(it.id, it.login, it.displayName, it.profileImageUrl)
                                }
                            )
                        }
                    )
                )
            }

            is DashGuildScopedRequest.CheckExternalGuildTwitchChannelRequest -> run {
                val twitchUser = getCachedUsersInfoByLogin(dashRequest.login)
                    .firstOrNull() ?: return@run DashGuildScopedResponse.CheckExternalGuildTwitchChannelResponse.UserNotFound

                val state = m.transaction { getTwitchAccountTrackState(twitchUser.id) }

                DashGuildScopedResponse.CheckExternalGuildTwitchChannelResponse.Success(
                    state,
                    TwitchUser(
                        twitchUser.id,
                        twitchUser.login,
                        twitchUser.displayName,
                        twitchUser.profileImageUrl
                    )
                )
            }

            is DashGuildScopedRequest.AddNewGuildTwitchChannelRequest -> {
                data class AddNewGuildTwitchChannelTransactionResult(
                    val valueOfTheDonationKeysEnabledOnThisGuild: Double,
                    val premiumTracksCount: Long,
                    val state: TwitchAccountTrackState
                )

                val transactionResult = m.transaction {
                    val state = getTwitchAccountTrackState(dashRequest.userId)

                    val valueOfTheDonationKeysEnabledOnThisGuild = DonationKey.find { DonationKeys.activeIn eq guild.idLong and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
                        .toList()
                        .sumOf { it.value }
                        .let { ceil(it) }

                    val premiumTracksCount = PremiumTrackTwitchAccounts.select {
                        PremiumTrackTwitchAccounts.guildId eq guild.idLong
                    }.count()

                    AddNewGuildTwitchChannelTransactionResult(
                        valueOfTheDonationKeysEnabledOnThisGuild,
                        premiumTracksCount,
                        state
                    )
                }

                val twitchUser = getCachedUsersInfoById(dashRequest.userId)
                    .first()

                DashGuildScopedResponse.AddNewGuildTwitchChannelResponse(
                    serializableGuild,
                    serializableSelfLorittaUser,
                    transactionResult.valueOfTheDonationKeysEnabledOnThisGuild,
                    transactionResult.premiumTracksCount,
                    transactionResult.state,
                    TwitchUser(
                        twitchUser.id,
                        twitchUser.login,
                        twitchUser.displayName,
                        twitchUser.profileImageUrl
                    )
                )
            }

            is DashGuildScopedRequest.EditGuildTwitchChannelRequest -> {
                data class EditGuildTwitchChannelTransactionResult(
                    val valueOfTheDonationKeysEnabledOnThisGuild: Double,
                    val premiumTracksCount: Long,
                    val trackedTwitchAccount: TrackedTwitchAccount,
                    val state: TwitchAccountTrackState
                )

                val transactionResult = m.newSuspendedTransaction {
                    val trackedAccount = TrackedTwitchAccounts.select {
                        (TrackedTwitchAccounts.guildId eq guild.idLong) and (TrackedTwitchAccounts.id eq dashRequest.trackedId)
                    }.first()

                    val valueOfTheDonationKeysEnabledOnThisGuild = DonationKey.find { DonationKeys.activeIn eq guild.idLong and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
                        .toList()
                        .sumOf { it.value }
                        .let { ceil(it) }

                    val premiumTracksCount = PremiumTrackTwitchAccounts.select {
                        PremiumTrackTwitchAccounts.guildId eq guild.idLong
                    }.count()

                    val twitchUserId = trackedAccount[TrackedTwitchAccounts.twitchUserId]

                    val state = getTwitchAccountTrackState(twitchUserId)

                    EditGuildTwitchChannelTransactionResult(
                        valueOfTheDonationKeysEnabledOnThisGuild,
                        premiumTracksCount,
                        TrackedTwitchAccount(
                            trackedAccount[TrackedTwitchAccounts.id].value,
                            trackedAccount[TrackedTwitchAccounts.twitchUserId],
                            trackedAccount[TrackedTwitchAccounts.channelId],
                            trackedAccount[TrackedTwitchAccounts.message],
                        ),
                        state
                    )
                }

                val twitchUser = getCachedUsersInfoById(transactionResult.trackedTwitchAccount.twitchUserId)
                    .first()

                DashGuildScopedResponse.EditGuildTwitchChannelResponse(
                    serializableGuild,
                    serializableSelfLorittaUser,
                    transactionResult.valueOfTheDonationKeysEnabledOnThisGuild,
                    transactionResult.premiumTracksCount,
                    transactionResult.trackedTwitchAccount,
                    transactionResult.state,
                    TwitchUser(
                        twitchUser.id,
                        twitchUser.login,
                        twitchUser.displayName,
                        twitchUser.profileImageUrl
                    )
                )
            }

            is DashGuildScopedRequest.UpsertGuildTwitchChannelRequest -> {
                val response = m.newSuspendedTransaction {
                    val updateCount = TrackedTwitchAccounts.update({
                        TrackedTwitchAccounts.id eq dashRequest.id and (TrackedTwitchAccounts.guildId eq guild.idLong)
                    }) {
                        it[TrackedTwitchAccounts.channelId] = dashRequest.channelId
                        it[TrackedTwitchAccounts.twitchUserId] = dashRequest.userId
                        it[TrackedTwitchAccounts.message] = dashRequest.message
                    }

                    if (updateCount == 0) {
                        // First we need to try creating the premium track, if needed
                        if (dashRequest.createPremiumTrack) {
                            val isAlreadyAdded = PremiumTrackTwitchAccounts.select {
                                PremiumTrackTwitchAccounts.guildId eq guild.idLong and (PremiumTrackTwitchAccounts.twitchUserId eq dashRequest.userId)
                            }.count() == 1L

                            if (!isAlreadyAdded) {
                                // We don't reeally care if there's already a premium track inserted
                                val valueOfTheDonationKeysEnabledOnThisGuild = DonationKey.find { DonationKeys.activeIn eq guild.idLong and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
                                    .toList()
                                    .sumOf { it.value }
                                    .let { ceil(it) }

                                val plan = ServerPremiumPlans.getPlanFromValue(valueOfTheDonationKeysEnabledOnThisGuild)

                                val premiumTracksOfTheGuildCount =
                                    PremiumTrackTwitchAccounts.slice(PremiumTrackTwitchAccounts.twitchUserId).select {
                                        PremiumTrackTwitchAccounts.guildId eq guild.idLong
                                    }.orderBy(
                                        PremiumTrackTwitchAccounts.addedAt,
                                        SortOrder.ASC
                                    ) // Ordered by the added at date...
                                        .count()

                                if (premiumTracksOfTheGuildCount >= plan.maxUnauthorizedTwitchChannels)
                                    return@newSuspendedTransaction DashGuildScopedResponse.UpsertGuildTwitchChannelResponse.TooManyPremiumTracks

                                PremiumTrackTwitchAccounts.insert {
                                    it[PremiumTrackTwitchAccounts.guildId] = guild.idLong
                                    it[PremiumTrackTwitchAccounts.twitchUserId] = dashRequest.userId
                                    it[PremiumTrackTwitchAccounts.addedBy] = user.idLong
                                    it[PremiumTrackTwitchAccounts.addedAt] = Instant.now()
                                }
                            }
                        }

                        // Does not exist, so let's insert it!
                        val trackedId = TrackedTwitchAccounts.insertAndGetId {
                            it[TrackedTwitchAccounts.guildId] = guild.idLong
                            it[TrackedTwitchAccounts.channelId] = dashRequest.channelId
                            it[TrackedTwitchAccounts.twitchUserId] = dashRequest.userId
                            it[TrackedTwitchAccounts.message] = dashRequest.message
                        }

                        return@newSuspendedTransaction DashGuildScopedResponse.UpsertGuildTwitchChannelResponse.Success(trackedId.value)
                    }

                    // The update count should NEVER be == 0 with a null ID
                    return@newSuspendedTransaction DashGuildScopedResponse.UpsertGuildTwitchChannelResponse.Success(dashRequest.id!!)
                }

                // Schedule subscription creation
                GlobalScope.launch {
                    m.makeRPCRequest<LorittaInternalRPCResponse.UpdateTwitchSubscriptionsResponse>(
                        m.lorittaMainCluster,
                        LorittaInternalRPCRequest.UpdateTwitchSubscriptionsRequest
                    )
                }

                response
            }

            is DashGuildScopedRequest.DeleteGuildTwitchChannelRequest -> {
                m.newSuspendedTransaction {
                    TrackedTwitchAccounts.deleteWhere {
                        TrackedTwitchAccounts.id eq dashRequest.trackedId and (TrackedTwitchAccounts.guildId eq guild.idLong)
                    }
                }
                DashGuildScopedResponse.DeleteGuildTwitchChannelResponse
            }

            is DashGuildScopedRequest.EnablePremiumTrackForTwitchChannelRequest -> {
                m.newSuspendedTransaction {
                    val isAlreadyAdded = PremiumTrackTwitchAccounts.select {
                        PremiumTrackTwitchAccounts.guildId eq guild.idLong and (PremiumTrackTwitchAccounts.twitchUserId eq dashRequest.userId)
                    }.count() == 1L

                    if (isAlreadyAdded)
                        return@newSuspendedTransaction DashGuildScopedResponse.EnablePremiumTrackForTwitchChannelResponse.AlreadyAdded

                    val valueOfTheDonationKeysEnabledOnThisGuild = DonationKey.find { DonationKeys.activeIn eq guild.idLong and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
                        .toList()
                        .sumOf { it.value }
                        .let { ceil(it) }

                    val plan = ServerPremiumPlans.getPlanFromValue(valueOfTheDonationKeysEnabledOnThisGuild)

                    val premiumTracksOfTheGuildCount = PremiumTrackTwitchAccounts.slice(PremiumTrackTwitchAccounts.twitchUserId).select {
                        PremiumTrackTwitchAccounts.guildId eq PremiumTrackTwitchAccounts.guildId
                    }.orderBy(PremiumTrackTwitchAccounts.addedAt, SortOrder.ASC) // Ordered by the added at date...
                        .count()

                    if (premiumTracksOfTheGuildCount >= plan.maxUnauthorizedTwitchChannels)
                        return@newSuspendedTransaction DashGuildScopedResponse.EnablePremiumTrackForTwitchChannelResponse.TooManyPremiumTracks

                    PremiumTrackTwitchAccounts.insert {
                        it[PremiumTrackTwitchAccounts.guildId] = guild.idLong
                        it[PremiumTrackTwitchAccounts.twitchUserId] = dashRequest.userId
                        it[PremiumTrackTwitchAccounts.addedBy] = user.idLong
                        it[PremiumTrackTwitchAccounts.addedAt] = Instant.now()
                    }
                    return@newSuspendedTransaction DashGuildScopedResponse.EnablePremiumTrackForTwitchChannelResponse.Success
                }
            }

            is DashGuildScopedRequest.DisablePremiumTrackForTwitchChannelRequest -> {
                m.newSuspendedTransaction {
                    PremiumTrackTwitchAccounts.deleteWhere {
                        PremiumTrackTwitchAccounts.guildId eq guild.idLong and (PremiumTrackTwitchAccounts.id eq dashRequest.premiumTrackedId)
                    }
                }

                DashGuildScopedResponse.DisablePremiumTrackForTwitchChannelResponse
            }
        }

        return LorittaInternalRPCResponse.ExecuteDashGuildScopedRPCResponse(dashResponse)
    }

    private fun getTwitchAccountTrackState(twitchUserId: Long): TwitchAccountTrackState {
        val isAuthorized = AuthorizedTwitchAccounts.select {
            AuthorizedTwitchAccounts.userId eq twitchUserId
        }.count() == 1L

        if (isAuthorized)
            return TwitchAccountTrackState.AUTHORIZED

        val isAlwaysTrack = AlwaysTrackTwitchAccounts.select {
            AlwaysTrackTwitchAccounts.userId eq twitchUserId
        }.count() == 1L

        if (isAlwaysTrack)
            return TwitchAccountTrackState.ALWAYS_TRACK_USER

        // Get if the premium track is enabled for this account, we need to check if any of the servers has a premium key enabled too
        val guildIds = PremiumTrackTwitchAccounts.slice(PremiumTrackTwitchAccounts.guildId).select {
            PremiumTrackTwitchAccounts.twitchUserId eq twitchUserId
        }.toList().map { it[PremiumTrackTwitchAccounts.guildId] }

        for (guildId in guildIds) {
            // This is a bit tricky to check, since we need to check what kind of plan the user has
            val valueOfTheDonationKeysEnabledOnThisGuild = DonationKey.find { DonationKeys.activeIn eq guildId and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
                .toList()
                .sumOf { it.value }
                .let { ceil(it) }

            val plan = ServerPremiumPlans.getPlanFromValue(valueOfTheDonationKeysEnabledOnThisGuild)

            if (plan.maxUnauthorizedTwitchChannels != 0) {
                // If the plan has a maxUnauthorizedTwitchChannels != 0, now we need to get ALL premium tracks of the guild...
                val allPremiumTracksOfTheGuild = PremiumTrackTwitchAccounts.slice(PremiumTrackTwitchAccounts.twitchUserId).select {
                    PremiumTrackTwitchAccounts.guildId eq PremiumTrackTwitchAccounts.guildId
                }.orderBy(PremiumTrackTwitchAccounts.addedAt, SortOrder.ASC) // Ordered by the added at date...
                    .limit(plan.maxUnauthorizedTwitchChannels) // Limited by the max unauthorized count...
                    .map { it[PremiumTrackTwitchAccounts.twitchUserId] } // Then we map by the twitch user ID...

                // And now, if the twitch User ID is in the list, then it means that...
                // 1. The guild is premium
                // 2. Has the user ID in the premium track
                // 3. And the plan fits the amount of premium tracks the user has
                if (twitchUserId in allPremiumTracksOfTheGuild)
                    return TwitchAccountTrackState.PREMIUM_TRACK_USER
            }
        }

        return TwitchAccountTrackState.UNAUTHORIZED
    }

    private suspend fun getCachedUsersInfoById(vararg ids: Long): List<net.perfectdreams.switchtwitch.data.TwitchUser> {
        // bye
        if (ids.isEmpty())
            return emptyList()

        val now24HoursAgo = Instant.now().minus(Duration.ofHours(24))

        val twitchUsers = mutableListOf<net.perfectdreams.switchtwitch.data.TwitchUser>()
        val idsToBeQueried = ids.toMutableList()

        // Get from our cache first
        val results = m.transaction {
            CachedTwitchChannels.select {
                CachedTwitchChannels.id inList idsToBeQueried and (CachedTwitchChannels.queriedAt greaterEq now24HoursAgo)
            }.toList()
        }

        for (result in results) {
            val data = result[CachedTwitchChannels.data]
            // If the data is null, then it means that the channel does not exist!
            if (data != null) {
                twitchUsers.add(Json.decodeFromString(data))
            }

            idsToBeQueried.remove(result[CachedTwitchChannels.id].value)
        }

        if (idsToBeQueried.isEmpty())
            return twitchUsers

        // Query anyone that wasn't matched by our cache!
        val queriedUsers = m.switchTwitch.getUsersInfoById(*idsToBeQueried.toLongArray())

        // And add to our cache
        if (queriedUsers.isNotEmpty()) {
            m.transaction {
                CachedTwitchChannels.batchUpsert(
                    queriedUsers,
                    CachedTwitchChannels.id,
                    shouldReturnGeneratedValues = false
                ) { it, item ->
                    it[CachedTwitchChannels.id] = item.id
                    it[CachedTwitchChannels.userLogin] = item.login
                    it[CachedTwitchChannels.data] = Json.encodeToString(item)
                    it[CachedTwitchChannels.queriedAt] = Instant.now()
                }
            }
        }

        twitchUsers += queriedUsers

        return twitchUsers
    }

    private suspend fun getCachedUsersInfoByLogin(vararg logins: String): List<net.perfectdreams.switchtwitch.data.TwitchUser> {
        // bye
        if (logins.isEmpty())
            return emptyList()

        val now24HoursAgo = Instant.now().minus(Duration.ofHours(24))

        val twitchUsers = mutableListOf<net.perfectdreams.switchtwitch.data.TwitchUser>()
        val idsToBeQueried = logins.toMutableList()

        // Get from our cache first
        val results = m.transaction {
            CachedTwitchChannels.select {
                CachedTwitchChannels.userLogin inList idsToBeQueried and (CachedTwitchChannels.queriedAt greaterEq now24HoursAgo)
            }.toList()
        }

        for (result in results) {
            val data = result[CachedTwitchChannels.data]
            // If the data is null, then it means that the channel does not exist!
            if (data != null) {
                twitchUsers.add(Json.decodeFromString(data))
            }

            idsToBeQueried.remove(result[CachedTwitchChannels.userLogin])
        }

        if (idsToBeQueried.isEmpty())
            return twitchUsers

        // Query anyone that wasn't matched by our cache!
        val queriedUsers = m.switchTwitch.getUsersInfoByLogin(*idsToBeQueried.toTypedArray())

        // And add to our cache
        if (queriedUsers.isNotEmpty()) {
            m.transaction {
                CachedTwitchChannels.batchUpsert(
                    queriedUsers,
                    CachedTwitchChannels.id,
                    shouldReturnGeneratedValues = false
                ) { it, item ->
                    it[CachedTwitchChannels.id] = item.id
                    it[CachedTwitchChannels.userLogin] = item.login
                    it[CachedTwitchChannels.data] = Json.encodeToString(item)
                    it[CachedTwitchChannels.queriedAt] = Instant.now()
                }
            }
        }

        twitchUsers += queriedUsers

        return twitchUsers
    }
}