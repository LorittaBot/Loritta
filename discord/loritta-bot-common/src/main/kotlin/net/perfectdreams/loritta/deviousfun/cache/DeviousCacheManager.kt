package net.perfectdreams.loritta.deviousfun.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import dev.kord.common.entity.*
import dev.kord.common.entity.optional.value
import it.unimi.dsi.fastutil.longs.Long2LongMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.serializer
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.utils.HashEncoder
import net.perfectdreams.loritta.deviouscache.data.*
import net.perfectdreams.loritta.deviousfun.DeviousShard
import net.perfectdreams.loritta.deviousfun.entities.*
import net.perfectdreams.loritta.deviousfun.events.guild.member.GuildMemberUpdateBoostTimeEvent
import net.perfectdreams.loritta.deviousfun.events.guild.member.GuildMemberUpdateNicknameEvent
import net.perfectdreams.loritta.deviousfun.events.user.UserUpdateAvatarEvent
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway
import net.perfectdreams.loritta.deviousfun.hooks.ListenerAdapter
import net.perfectdreams.loritta.deviousfun.utils.*
import org.jetbrains.exposed.sql.Database
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Manages cache
 */
class DeviousCacheManager(
    val m: DeviousShard,
    val database: Database,
    val triggeredEventsDueToCacheUpdate: kotlinx.coroutines.channels.Channel<(DeviousShard) -> (Unit)>,
    val users: SnowflakeMap<DeviousUserData>,
    val guilds: SnowflakeMap<DeviousGuildDataWrapper>,
    val guildChannels: SnowflakeMap<SnowflakeMap<DeviousChannelData>>,
    val channelsToGuilds: Long2LongMap,
    val emotes: SnowflakeMap<SnowflakeMap<DeviousGuildEmojiData>>,
    val roles: SnowflakeMap<SnowflakeMap<DeviousRoleData>>,
    val members: SnowflakeMap<SnowflakeMap<DeviousMemberData>>,
    val voiceStates: SnowflakeMap<SnowflakeMap<DeviousVoiceStateData>>,
    var gatewaySession: DeviousGatewaySession?,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val deviousGateway: DeviousGateway
        get() = m.deviousGateway

    /**
     * Checks if this DeviousCacheManager instance is active
     *
     * If this is false, all cache requests should be ignored and new data shouldn't be added to the maps
     */
    var isActive = true

    val cacheDatabase = DeviousCacheDatabase(this, database)

    // Serialized Hashes of Entities
    // Useful to avoid acquiring a Redis connection when there wasn't any changes in the entity itself
    private val cachedUserHashes = Caffeine.newBuilder()
        .expireAfterAccess(15L, TimeUnit.MINUTES)
        .build<Snowflake, Int>()
    private val cachedMemberHashes = Caffeine.newBuilder()
        .expireAfterAccess(15L, TimeUnit.MINUTES)
        .build<Snowflake, Int>()
    // Entity specific mutexes
    val mutexes = ConcurrentHashMap<EntityKey, Mutex>()

    // A mutex, kind of
    private val entityPersistenceModificationMutex = MutableStateFlow<CacheEntityStatus>(CacheEntityStatus.OK)

    suspend fun getGuild(id: Snowflake): Guild? {
        val lightweightSnowflake = id.toLightweightSnowflake()
        withLock(GuildKey(lightweightSnowflake)) {
            logger.debug { "Getting guild + entities with ID $id" }

            val cachedGuild = guilds[lightweightSnowflake] ?: return null
            val cachedGuildData = cachedGuild.data
            val roles = roles[lightweightSnowflake]?.toMap() ?: emptyMap()
            val channels = guildChannels[lightweightSnowflake]?.toMap() ?: emptyMap()
            val emojis = emotes[lightweightSnowflake]?.toMap() ?: emptyMap()

            val cacheWrapper = Guild.CacheWrapper()
            val guild = Guild(
                m,
                cachedGuildData,
                cacheWrapper
            )

            cacheWrapper.roles.putAll(
                roles.map { (id, data) ->
                    id.toKordSnowflake() to Role(
                        m,
                        guild,
                        data
                    )
                }
            )

            cacheWrapper.channels.putAll(
                channels.map { (id, data) ->
                    id.toKordSnowflake() to Channel(
                        m,
                        guild,
                        data
                    )
                }
            )

            cacheWrapper.emotes.putAll(
                emojis.map { (id, data) ->
                    id.toKordSnowflake() to DiscordGuildEmote(
                        m,
                        guild,
                        data
                    )
                }
            )

            return guild
        }
    }

    suspend fun getGuildFailIfSnowflakeIsNotNullButGuildIsNotPresent(id: Snowflake?): GuildResult {
        if (id == null)
            return GuildResult.NullSnowflake

        val guild = getGuild(id)
        if (guild != null)
            return GuildResult.GuildPresent(guild)
        return GuildResult.GuildNotPresent
    }

    sealed class GuildResult {
        class GuildPresent(val guild: Guild) : GuildResult()
        object NullSnowflake : GuildResult()
        object GuildNotPresent : GuildResult()
    }

    suspend fun createGuild(
        data: DiscordGuild,
        guildChannelsAndThreads: List<DiscordChannel>?,
    ): Guild {
        val lightweightSnowflake = data.id.toLightweightSnowflake()

        withLock(GuildKey(lightweightSnowflake)) {
            val cachedGuild = guilds[lightweightSnowflake]
            val deviousGuildData = DeviousGuildData.from(
                data,
                data.premiumSubscriptionCount.value ?: cachedGuild?.data?.premiumSubscriptionCount ?: error("Could not get the premiumSubscriptionCount for ${data.id}! Cached Guild Data: $cachedGuild"),
                data.memberCount.value ?: cachedGuild?.data?.memberCount ?: error("Could not get the memberCount for ${data.id}! Cached Guild Data: $cachedGuild")
            )
            val guildMembers = data.members.value
            val guildVoiceStates = data.voiceStates.value

            val rolesData = data.roles.map { DeviousRoleData.from(it) }
            val emojisData = data.emojis.map { DeviousGuildEmojiData.from(it) }
            val channelsData = guildChannelsAndThreads?.map { DeviousChannelData.from(data.id, it) }
            val membersData = guildMembers?.associate { it.user.value!!.id.toLightweightSnowflake() to DeviousMemberData.from(it) }
            val voiceStatesData = guildVoiceStates?.map { DeviousVoiceStateData.from(it) }

            awaitForEntityPersistenceModificationMutex()

            // We are going to execute everything at the same time
            val cacheActions = mutableListOf<DeviousCacheDatabase.DirtyEntitiesWrapper.() -> (Unit)>()

            if (guildMembers != null) {
                for (member in guildMembers) {
                    val userData = member.user.value!!
                    val deviousUserData = DeviousUserData.from(userData)
                    val lightweightUserId = userData.id.toLightweightSnowflake()
                    users[lightweightUserId] = deviousUserData

                    cacheActions.add {
                        this.users[lightweightUserId] = DatabaseCacheValue.Value(deviousUserData)
                    }
                }
            }

            // logger.info { "Updating guild with ID $lightweightSnowflake" }

            val wrapper = DeviousGuildDataWrapper(deviousGuildData)
            guilds[lightweightSnowflake] = wrapper

            cacheActions.add {
                this.guilds[lightweightSnowflake] = DatabaseCacheValue.Value(wrapper)
            }

            val currentEmotes = emotes[lightweightSnowflake]
            runIfDifferentAndNotNull(currentEmotes?.values, emojisData) {
                val newEmojis = SnowflakeMap(it.associateBy { it.id })
                emotes[lightweightSnowflake] = newEmojis

                cacheActions.add {
                    this.emojis[lightweightSnowflake] = DatabaseCacheValue.Value(newEmojis.toMap())
                }
            }

            val currentRoles = roles[lightweightSnowflake]
            runIfDifferentAndNotNull(currentRoles?.values, rolesData) {
                val newRoles = SnowflakeMap(it.associateBy { it.id })
                roles[lightweightSnowflake] = newRoles

                cacheActions.add {
                    this.roles[lightweightSnowflake] = DatabaseCacheValue.Value(newRoles.toMap())
                }
            }

            if (membersData != null) {
                val currentMembers = members[lightweightSnowflake]
                // The expected size will be the size of the members data map
                members[lightweightSnowflake] = (currentMembers ?: SnowflakeMap(membersData.size))
                    .also {
                        for ((id, member) in membersData) {
                            val currentMember = it[id]
                            if (currentMember != member) {
                                it[id] = member
                                cacheActions.add {
                                    this.members[GuildAndUserPair(lightweightSnowflake, id)] = DatabaseCacheValue.Value(member)
                                }
                            }
                        }
                    }
            }

            if (channelsData != null) {
                val currentChannels = this.guildChannels[lightweightSnowflake]
                runIfDifferentAndNotNull(currentChannels?.values, channelsData) {
                    val newChannels = SnowflakeMap(it.associateBy { it.id })
                    this.guildChannels[lightweightSnowflake] = newChannels

                    cacheActions.add {
                        this.guildChannels[lightweightSnowflake] = DatabaseCacheValue.Value(newChannels.toMap())
                    }
                }

                val oldChannelIds = currentChannels?.keys?.map { LightweightSnowflake(it) }?.toSet()

                // Remove removed channels from the global channel cache and from the fast channel map
                if (oldChannelIds != null) {
                    for (channelId in (channelsData.map { it.id } - oldChannelIds)) {
                        this.guildChannels.remove(channelId)
                        this.channelsToGuilds.remove(channelId.value.toLong())
                        cacheActions.add {
                            this.guildChannels[channelId] = DatabaseCacheValue.Null()
                        }
                    }
                }

                // Add all channels to the fast channel map cache
                val channelMappedByIds = channelsData.associateBy { it.id }

                for ((channelId, newChannel) in channelMappedByIds) {
                    this.channelsToGuilds[channelId.value.toLong()] = lightweightSnowflake.value.toLong()
                }
            }

            val currentVoiceStates = voiceStates[lightweightSnowflake]
            runIfDifferentAndNotNull(currentVoiceStates?.values, voiceStatesData) {
                val cachedVoiceStates = SnowflakeMap(it.associateBy { it.userId })
                voiceStates[lightweightSnowflake] = cachedVoiceStates
                cacheActions.add {
                    this.voiceStates[lightweightSnowflake] = DatabaseCacheValue.Value(cachedVoiceStates.toMap())
                }
            }

            // Trigger all cache actions in the same callback
            cacheDatabase.queue {
                cacheActions.forEach {
                    it.invoke(this)
                }
            }

            val cacheWrapper = Guild.CacheWrapper()
            val guild = Guild(
                m,
                deviousGuildData,
                cacheWrapper
            )

            if (channelsData != null) {
                for (channelData in channelsData) {
                    cacheWrapper.channels[channelData.id.toKordSnowflake()] = Channel(
                        m,
                        guild,
                        channelData
                    )
                }
            }

            for (roleData in rolesData) {
                cacheWrapper.roles[roleData.id.toKordSnowflake()] = Role(
                    m,
                    guild,
                    roleData
                )
            }

            for (emojiData in emojisData) {
                cacheWrapper.emotes[emojiData.id.toKordSnowflake()] = DiscordGuildEmote(
                    m,
                    guild,
                    emojiData
                )
            }

            return guild
        }
    }

    suspend fun deleteGuild(guildId: Snowflake) {
        val lightweightSnowflake = guildId.toLightweightSnowflake()
        withLock(GuildKey(lightweightSnowflake)) {
            awaitForEntityPersistenceModificationMutex()

            logger.debug { "Deleting guild with ID $lightweightSnowflake" }

            val cachedChannels = guildChannels[lightweightSnowflake]

            roles.remove(lightweightSnowflake)
            emotes.remove(lightweightSnowflake)
            guildChannels.remove(lightweightSnowflake)
            guilds.remove(lightweightSnowflake)
            voiceStates.remove(lightweightSnowflake)

            if (cachedChannels != null) {
                for (channel in cachedChannels) {
                    this.channelsToGuilds.remove(channel.first.value.toLong())
                }
            }

            val members = members[lightweightSnowflake]
            if (members != null)
                this.members.remove(lightweightSnowflake)

            cacheDatabase.queue {
                roles[lightweightSnowflake] = DatabaseCacheValue.Null()
                emojis[lightweightSnowflake] = DatabaseCacheValue.Null()
                guildChannels[lightweightSnowflake] = DatabaseCacheValue.Null()

                members?.forEach { memberId, _ ->
                    // Bust the cache of all the members on this guild
                    this.members[GuildAndUserPair(lightweightSnowflake, memberId)] = DatabaseCacheValue.Null()
                }

                this.voiceStates[lightweightSnowflake] = DatabaseCacheValue.Null()
            }
        }
    }

    suspend fun storeEmojis(guildId: Snowflake, emojis: List<DeviousGuildEmojiData>) {
        val lightweightSnowflake = guildId.toLightweightSnowflake()

        withLock(GuildKey(lightweightSnowflake)) {
            awaitForEntityPersistenceModificationMutex()

            logger.debug { "Updating guild emojis on guild $lightweightSnowflake" }

            val newEmotes = SnowflakeMap(emojis.associateBy { it.id })
            emotes[lightweightSnowflake] = newEmotes
            val newEmotesAsMap = newEmotes.toMap()

            cacheDatabase.queue {
                this.emojis[lightweightSnowflake] = DatabaseCacheValue.Value(newEmotesAsMap)
            }
        }
    }

    suspend fun getUser(id: Snowflake): User? {
        val lightweightSnowflake = id.toLightweightSnowflake()

        withLock(UserKey(lightweightSnowflake)) {
            logger.debug { "Getting user with ID $lightweightSnowflake" }
            val deviousUserData = users[lightweightSnowflake] ?: return null

            return User(
                m,
                id,
                deviousUserData
            )
        }
    }

    suspend fun createUser(user: DiscordUser, addToCache: Boolean): User {
        val deviousUserData = DeviousUserData.from(user)

        val deviousUser = User(m, user.id, deviousUserData)
        if (addToCache) {
            doIfNotMatch(cachedUserHashes, user.id, user) {
                val lightweightSnowflake = user.id.toLightweightSnowflake()
                var oldUser: DeviousUserData? = null

                withLock(UserKey(lightweightSnowflake)) {
                    awaitForEntityPersistenceModificationMutex()

                    logger.debug { "Updating user with ID $lightweightSnowflake" }
                    oldUser = users[lightweightSnowflake]
                    users[lightweightSnowflake] = deviousUserData

                    cacheDatabase.queue {
                        this.users[lightweightSnowflake] = DatabaseCacheValue.Value(deviousUserData)
                    }
                }

                // Let's compare the old member x new member data to trigger events
                val oldUserData = oldUser
                val newUserData = deviousUserData

                if (oldUserData != null) {
                    val oldAvatarId = oldUserData.avatar
                    val newAvatarId = newUserData.avatar

                    if (oldAvatarId != newAvatarId) {
                        triggeredEventsDueToCacheUpdate.send {
                            m.forEachListeners(
                                UserUpdateAvatarEvent(
                                    m,
                                    m.deviousGateway,
                                    deviousUser,
                                    oldAvatarId,
                                    newAvatarId
                                ),
                                ListenerAdapter::onUserUpdateAvatar
                            )
                        }
                    }
                }
            }
        }

        return deviousUser
    }

    suspend fun getMember(user: User, guild: Guild): Member? {
        val guildId = guild.idSnowflake.toLightweightSnowflake()
        val userId = user.idSnowflake.toLightweightSnowflake()

        withLock(GuildKey(guildId), UserKey(userId)) {
            logger.debug { "Getting guild member $userId of guild $guildId" }

            val cachedMembers = members[guildId] ?: return null
            val cachedMember = cachedMembers[userId] ?: return null

            return Member(
                m,
                cachedMember,
                guild,
                user
            )
        }
    }

    suspend fun createMember(user: User, guild: Guild, member: DiscordGuildMember) =
        createMember(user, guild, DeviousMemberData.from(member))

    suspend fun createMember(user: User, guild: Guild, member: DiscordAddedGuildMember) =
        createMember(user, guild, DeviousMemberData.from(member))

    suspend fun createMember(user: User, guild: Guild, member: DiscordUpdatedGuildMember): Member {
        // Because the DiscordUpdatedGuildMember entity does not have some fields, we will use them as a copy
        val oldDeviousMemberData = getMember(user, guild)?.member
        return createMember(user, guild, DeviousMemberData.from(member, oldDeviousMemberData))
    }

    suspend fun createMember(user: User, guild: Guild, deviousMemberData: DeviousMemberData): Member {
        val member = Member(
            m,
            deviousMemberData,
            guild,
            user
        )

        val guildId = guild.idSnowflake.toLightweightSnowflake()
        val userId = user.idSnowflake.toLightweightSnowflake()

        doIfNotMatch(cachedMemberHashes, user.idSnowflake, deviousMemberData) {
            var oldMember: DeviousMemberData? = null

            withLock(GuildKey(guildId), UserKey(userId)) {
                awaitForEntityPersistenceModificationMutex()

                logger.debug { "Updating guild member with ID ${userId} on guild ${guildId}" }

                val currentMembers = members[guildId]
                // Expected 1 because we will insert the new member
                members[guildId] = (currentMembers ?: SnowflakeMap(1))
                    .also {
                        oldMember = it[userId]
                        it[userId] = deviousMemberData
                    }

                cacheDatabase.queue {
                    this.members[GuildAndUserPair(guildId, userId)] = DatabaseCacheValue.Value(deviousMemberData)
                }
            }

            // Let's compare the old member x new member data to trigger events
            val oldMemberData = oldMember
            val newMemberData = deviousMemberData

            // Yes, I know, this isn't within the != null check
            // JDA also handles update boost time in the same way
            val oldTimeBoosted = oldMemberData?.premiumSince
            val newTimeBoosted = newMemberData.premiumSince

            if (oldTimeBoosted != newTimeBoosted) {
                triggeredEventsDueToCacheUpdate.send {
                    m.forEachListeners(
                        GuildMemberUpdateBoostTimeEvent(
                            m,
                            m.deviousGateway,
                            guild,
                            user,
                            member,
                            oldTimeBoosted?.toJavaInstant()?.atOffset(ZoneOffset.UTC),
                            newTimeBoosted?.toJavaInstant()?.atOffset(ZoneOffset.UTC)
                        ),
                        ListenerAdapter::onGuildMemberUpdateBoostTime
                    )
                }
            }

            if (oldMemberData != null) {
                val oldNickname = oldMemberData.nick
                val newNickname = newMemberData.nick

                if (oldNickname != newNickname) {
                    triggeredEventsDueToCacheUpdate.send {
                        m.forEachListeners(
                            GuildMemberUpdateNicknameEvent(
                                m,
                                m.deviousGateway,
                                guild,
                                user,
                                member,
                                oldNickname,
                                newNickname
                            ),
                            ListenerAdapter::onGuildMemberUpdateNickname
                        )
                    }
                }
            }
        }

        return member
    }

    suspend fun deleteMember(guild: Guild, userId: Snowflake) {
        val guildId = guild.idSnowflake.toLightweightSnowflake()
        val userId = userId.toLightweightSnowflake()

        withLock(GuildKey(guildId), UserKey(userId)) {
            awaitForEntityPersistenceModificationMutex()

            logger.debug { "Deleting guild member with ID $userId on guild $guildId" }

            val currentMembers = members[guildId]
            members[guildId] = (currentMembers ?: SnowflakeMap(0))
                .also {
                    it.remove(userId)
                }

            cacheDatabase.queue {
                this.members[GuildAndUserPair(guildId, userId)] = DatabaseCacheValue.Null()
            }
        }
    }

    suspend fun createRole(guild: Guild, role: DiscordRole): Role {
        val data = DeviousRoleData.from(role)

        val guildId = guild.idSnowflake.toLightweightSnowflake()

        withLock(GuildKey(guildId)) {
            awaitForEntityPersistenceModificationMutex()

            logger.debug { "Updating guild role with ID ${data.id} on guild $guildId" }

            val currentRoles = roles[guildId]
            // Expected 1 because we will insert the new role
            val newRoles = (currentRoles ?: SnowflakeMap(1))
                .also {
                    it[data.id] = data
                }
            val newRolesCloneAsMap = newRoles.toMap()
            roles[guildId] = newRoles

            cacheDatabase.queue {
                this.roles[guildId] = DatabaseCacheValue.Value(newRolesCloneAsMap)
            }
        }

        return Role(
            m,
            guild,
            data
        )
    }

    suspend fun deleteRole(guild: Guild, roleId: Snowflake) {
        val guildId = guild.idSnowflake.toLightweightSnowflake()
        val roleId = roleId.toLightweightSnowflake()

        // It seems that deleting a role does trigger a member update related to the role removal, so we won't need to manually remove it (yay)
        withLock(GuildKey(guildId)) {
            awaitForEntityPersistenceModificationMutex()

            logger.debug { "Deleting guild role with ID ${roleId} on guild ${guildId}" }

            val currentRoles = roles[guildId]
            val newRoles = (currentRoles ?: SnowflakeMap(0))
                .also {
                    it.remove(roleId)
                }
            val newRolesCloneAsMap = newRoles.toMap()
            roles[guildId] = newRoles

            cacheDatabase.queue {
                this.roles[roleId] = DatabaseCacheValue.Value(newRolesCloneAsMap)
            }
        }
    }

    suspend fun getChannel(channelId: Snowflake): Channel? {
        val channelId = channelId.toLightweightSnowflake()
        withLock(ChannelKey(channelId)) {
            logger.debug { "Getting channel $channelId" }

            if (!channelsToGuilds.containsKey(channelId.value.toLong()))
                return null // Unknown channel

            val guildId = LightweightSnowflake(channelsToGuilds.get(channelId.value.toLong()))
            val cachedGuild = guilds[guildId] ?: return null
            val cachedChannels = guildChannels[guildId] ?: return null
            val cachedChannel = cachedChannels[channelId] ?: return null
            val cachedGuildData = cachedGuild.data
            val roles = roles[guildId]?.toMap() ?: emptyMap()
            val channels = guildChannels[guildId]?.toMap() ?: emptyMap()
            val emojis = emotes[guildId]?.toMap() ?: emptyMap()

            val cacheWrapper = Guild.CacheWrapper()
            val guild = Guild(
                m,
                cachedGuildData,
                cacheWrapper
            )

            cacheWrapper.roles.putAll(
                roles.map { (id, data) ->
                    id.toKordSnowflake() to Role(
                        m,
                        guild,
                        data
                    )
                }
            )

            cacheWrapper.channels.putAll(
                channels.map { (id, data) ->
                    id.toKordSnowflake() to Channel(
                        m,
                        guild,
                        data
                    )
                }
            )

            cacheWrapper.emotes.putAll(
                emojis.map { (id, data) ->
                    id.toKordSnowflake() to DiscordGuildEmote(
                        m,
                        guild,
                        data
                    )
                }
            )

            return Channel(m, guild, cachedChannel)
        }
    }

    suspend fun createChannel(guild: Guild, data: DiscordChannel): Channel {
        val data = DeviousChannelData.from(guild.idSnowflake, data)

        val guildId = guild.idSnowflake.toLightweightSnowflake()

        withLock(GuildKey(guildId), ChannelKey(data.id)) {
            awaitForEntityPersistenceModificationMutex()

            logger.debug { "Updating guild channel with ID ${data.id} on guild $guildId" }

            val currentChannels = guildChannels[guildId]
            // Expected 1 because we will insert the new role
            val newChannels = (currentChannels ?: SnowflakeMap(1))
                .also {
                    it[data.id] = data
                }
            val newChannelsCloneAsMap = newChannels.toMap()
            guildChannels[guildId] = newChannels
            channelsToGuilds[data.id.value.toLong()] = guild.idLong

            cacheDatabase.queue {
                this.guildChannels[guildId] = DatabaseCacheValue.Value(newChannelsCloneAsMap)
            }
        }

        return Channel(
            m,
            guild,
            data
        )
    }

    suspend fun deleteChannel(guild: Guild, channelId: Snowflake) {
        val guildId = guild.idSnowflake.toLightweightSnowflake()
        val channelId = channelId.toLightweightSnowflake()

        withLock(GuildKey(guildId), ChannelKey(channelId)) {
            awaitForEntityPersistenceModificationMutex()

            logger.debug { "Deleting guild channel with ID $channelId on guild $guildId" }

            val currentChannels = guildChannels[guildId]
            val newChannels = (currentChannels ?: SnowflakeMap(0))
                .also {
                    it.remove(channelId)
                }
            val newChannelsCloneAsMap = newChannels.toMap()
            guildChannels[guildId] = newChannels
            this.channelsToGuilds.remove(channelId.value.toLong())

            cacheDatabase.queue {
                this.guildChannels[channelId] = DatabaseCacheValue.Value(newChannelsCloneAsMap)
            }
        }
    }

    /**
     * Hashes [value]'s primitives with [Objects.hash] to create a hash that identifies the object.
     */
    private inline fun <reified T> hashEntity(value: T): Int {
        // We use our own custom hash encoder because ProtoBuf can't encode the "Optional" fields, because it can't serialize null values
        // on a field that isn't marked as null
        val encoder = HashEncoder()
        encoder.encodeSerializableValue(serializer(), value)
        return Objects.hash(*encoder.list.toTypedArray())
    }

    private inline fun <reified T> doIfNotMatch(
        cache: Cache<Snowflake, Int>,
        id: Snowflake,
        data: T,
        actionIfNotMatch: () -> (Unit)
    ) {
        val hashedEntity = hashEntity(data)
        if (cache.getIfPresent(id) != hashedEntity) {
            actionIfNotMatch.invoke()
            cache.put(id, hashedEntity)
        }
    }

    /**
     * Locks the [entityKeys] for manipulation.
     *
     * The mutexes are locked on the following order:
     * * Guild
     * * Channel
     * * User
     * and the IDs are sorted per category from smallest to largest.
     *
     * This order is necessary to avoid deadlocking when two coroutines invoke withLock at the same time!
     */
    suspend inline fun <T> withLock(vararg entityKeys: EntityKey, action: () -> (T)): T {
        val sortedEntityKeys = mutableListOf<EntityKey>()

        for (entityKey in entityKeys) {
            if (entityKey is GuildKey)
                sortedEntityKeys.add(entityKey)
        }

        for (entityKey in entityKeys) {
            if (entityKey is ChannelKey)
                sortedEntityKeys.add(entityKey)
        }

        for (entityKey in entityKeys) {
            if (entityKey is UserKey)
                sortedEntityKeys.add(entityKey)
        }

        val mutexesToBeLocked = sortedEntityKeys
            .sortedBy { it.id }
            .map { mutexes.getOrPut(it) { Mutex() } }

        for (mutex in mutexesToBeLocked) {
            mutex.lock()
        }

        try {
            return action.invoke()
        } finally {
            for (mutex in mutexesToBeLocked) {
                mutex.unlock()
            }
        }
    }

    suspend fun awaitForEntityPersistenceModificationMutex() {
        // Wait until it is ok before proceeding
        entityPersistenceModificationMutex
            .filter {
                it == CacheEntityStatus.OK
            }
            .first()
    }

    suspend fun stop() {
        if (!isActive)
            return

        isActive = false

        // Shutdown
        cacheDatabase.stop()
        users.clear()
        guilds.clear()
        guildChannels.clear()
        channelsToGuilds.clear()
        emotes.clear()
        roles.clear()
        members.clear()
        voiceStates.clear()
        gatewaySession = null
    }
}