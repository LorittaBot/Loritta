package net.perfectdreams.loritta.deviousfun.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import dev.kord.common.entity.*
import kotlinx.coroutines.sync.withPermit
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.serializer
import net.perfectdreams.loritta.cinnamon.pudding.utils.HashEncoder
import net.perfectdreams.loritta.deviouscache.data.*
import net.perfectdreams.loritta.deviouscache.requests.*
import net.perfectdreams.loritta.deviouscache.responses.*
import net.perfectdreams.loritta.deviousfun.DeviousFun
import net.perfectdreams.loritta.deviousfun.entities.*
import net.perfectdreams.loritta.deviousfun.events.guild.member.GuildMemberUpdateBoostTimeEvent
import net.perfectdreams.loritta.deviousfun.hooks.ListenerAdapter
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Manages cache
 */
class DeviousCacheManager(val m: DeviousFun) {
    private val binaryCacheTransformers = m.loritta.binaryCacheTransformers

    // Serialized Hashes of Entities
    // Useful to avoid acquiring a Redis connection when there wasn't any changes in the entity itself
    private val cachedUserHashes = Caffeine.newBuilder()
        .expireAfterAccess(15L, TimeUnit.MINUTES)
        .build<Snowflake, Int>()
    private val cachedMemberHashes = Caffeine.newBuilder()
        .expireAfterAccess(15L, TimeUnit.MINUTES)
        .build<Snowflake, Int>()


    suspend fun getGuild(id: Snowflake): Guild? {
        val response = m.rpc.execute(GetGuildWithEntitiesRequest(id))

        return when (response) {
            is GetGuildWithEntitiesResponse -> {
                val cacheWrapper = Guild.CacheWrapper()
                val guild = Guild(
                    m,
                    response.data,
                    cacheWrapper
                )

                cacheWrapper.roles.putAll(
                    response.roles.mapValues { (_, data) ->
                        Role(
                            m,
                            guild,
                            data
                        )
                    }
                )

                cacheWrapper.channels.putAll(
                    response.channels.mapValues { (_, data) ->
                        Channel(
                            m,
                            guild,
                            data
                        )
                    }
                )

                cacheWrapper.emotes.putAll(
                    response.emojis.mapValues { (_, data) ->
                        DiscordGuildEmote(
                            m,
                            guild,
                            data
                        )
                    }
                )

                return guild
            }

            NotFoundResponse -> null
            else -> m.rpc.unknownResponse(response)
        }
    }

    suspend fun createGuild(
        data: DiscordGuild,
        guildChannels: List<DiscordChannel>?,
    ): Guild {
        m.guildCreateSemaphore.withPermit {
            val deviousGuildData = DeviousGuildData.from(data)
            val guildMembers = data.members.value
            val guildVoiceStates = data.voiceStates.value

            val rolesData = data.roles.map { DeviousRoleData.from(it) }
            val emojisData = data.emojis.map { DeviousGuildEmojiData.from(it) }

            m.rpc.execute(
                PutGuildRequest(
                    data.id,
                    deviousGuildData,
                    rolesData,
                    emojisData,
                    guildMembers?.associate { it.user.value!!.id to DeviousMemberData.from(it) },
                    guildChannels?.map { DeviousChannelData.from(data.id, it) },
                    guildVoiceStates?.map { DeviousVoiceStateData.from(it) }
                )
            )

            val cacheWrapper = Guild.CacheWrapper()
            val guild = Guild(
                m,
                deviousGuildData,
                cacheWrapper
            )

            for (roleData in rolesData) {
                cacheWrapper.roles[roleData.id] = Role(
                    m,
                    guild,
                    roleData
                )
            }

            for (emojiData in emojisData) {
                cacheWrapper.emotes[emojiData.id] = DiscordGuildEmote(
                    m,
                    guild,
                    emojiData
                )
            }

            return guild
        }
    }

    suspend fun deleteGuild(guildId: Snowflake) {
        m.rpc.execute(DeleteGuildRequest(guildId))
        // TODO: Check if all of these are correctly handled
        /* val channelsOfThisGuild = m.loritta.redisConnection("get channel IDs of guild $guildId for deletion") {
            it.smembers(m.loritta.redisKeys.discordGuildChannels(guildId))
        }

        m.loritta.redisTransaction("delete guild $guildId") {
            it.hdel(m.loritta.redisKeys.discordGuilds(), guildId.toString())
            it.del(m.loritta.redisKeys.discordGuildMembers(guildId))
            it.del(m.loritta.redisKeys.discordGuildRoles(guildId))
            it.del(m.loritta.redisKeys.discordGuildChannels(guildId))
            it.del(m.loritta.redisKeys.discordGuildEmojis(guildId))
            it.del(m.loritta.redisKeys.discordGuildVoiceStates(guildId))
            it.hdel(m.loritta.redisKeys.discordChannels(), *channelsOfThisGuild.toTypedArray())
        } */
    }

    suspend fun storeEmojis(guildId: Snowflake, emojis: List<DeviousGuildEmojiData>) {
        // Upsert emojis
        m.rpc.execute(PutGuildEmojisRequest(guildId, emojis))
    }

    suspend fun getUser(id: Snowflake): User? {
        val response = m.rpc.execute(GetUserRequest(id))

        return when (response) {
            is GetUserResponse -> User(
                m,
                id,
                response.user
            )

            NotFoundResponse -> null
            else -> m.rpc.unknownResponse(response)
        }
    }

    suspend fun createUser(user: DiscordUser, addToCache: Boolean): User {
        val deviousUserData = DeviousUserData.from(user)

        if (addToCache) {
            m.rpc.execute(PutUserRequest(user.id, deviousUserData))
        }

        return User(m, user.id, deviousUserData)
    }

    suspend fun getMember(user: User, guild: Guild): Member? {
        val memberData = (m.rpc.execute(
            GetGuildMemberRequest(
                guild.idSnowflake,
                user.idSnowflake
            )
        ) as? GetGuildMemberResponse)?.member ?: return null

        return Member(
            m,
            memberData,
            guild,
            user
        )
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

        doIfNotMatch(cachedMemberHashes, user.idSnowflake, deviousMemberData) {
            // Update the user member data
            when (val response =
                m.rpc.execute(PutGuildMemberRequest(guild.idSnowflake, user.idSnowflake, deviousMemberData))) {
                is PutGuildMemberResponse -> {
                    // Let's compare the old member x new member data to trigger events
                    val oldMemberData = response.oldMember
                    val newMemberData = response.newMember

                    if (oldMemberData != null) {
                        val oldTimeBoosted = oldMemberData.premiumSince
                        val newTimeBoosted = newMemberData.premiumSince

                        if (oldTimeBoosted != newTimeBoosted) {
                            m.forEachListeners(
                                GuildMemberUpdateBoostTimeEvent(
                                    m,
                                    // Because we don't have access to the gateway instance here, let's get the gateway manually
                                    // This needs to be refactored later, because some events (example: user update) may not have a specific gateway bound to it
                                    m.gatewayManager.getGatewayForGuild(guild.idSnowflake),
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
                }

                is NotFoundResponse -> {}
                else -> m.rpc.unknownResponse(response)
            }
        }

        return member
    }

    suspend fun deleteMember(guild: Guild, userId: Snowflake) {
        m.rpc.execute(DeleteGuildMemberRequest(guild.idSnowflake, userId))
    }

    suspend fun createRole(guild: Guild, role: DiscordRole): Role {
        val data = DeviousRoleData.from(role)

        m.rpc.execute(PutGuildRoleRequest(guild.idSnowflake, data))

        return Role(
            m,
            guild,
            data
        )
    }

    suspend fun deleteRole(guild: Guild, roleId: Snowflake) {
        // It seems that deleting a role does trigger a member update related to the role removal, so we won't need to manually remove it (yay)
        m.rpc.execute(DeleteGuildRoleRequest(guild.idSnowflake, roleId))
    }

    suspend fun getChannel(channelId: Snowflake): Channel? {
        return when (val response = m.rpc.execute(GetChannelRequest(channelId))) {
            is GetGuildChannelResponse -> {
                val cacheWrapper = Guild.CacheWrapper()
                val guild = Guild(
                    m,
                    response.data,
                    cacheWrapper
                )

                cacheWrapper.roles.putAll(
                    response.roles.mapValues { (_, data) ->
                        Role(
                            m,
                            guild,
                            data
                        )
                    }
                )

                cacheWrapper.channels.putAll(
                    response.channels.mapValues { (_, data) ->
                        Channel(
                            m,
                            guild,
                            data
                        )
                    }
                )

                cacheWrapper.emotes.putAll(
                    response.emojis.mapValues { (_, data) ->
                        DiscordGuildEmote(
                            m,
                            guild,
                            data
                        )
                    }
                )

                return Channel(m, guild, response.channel)
            }

            is GetChannelResponse -> {
                return Channel(m, null, response.channel)
            }

            NotFoundResponse -> null
            else -> m.rpc.unknownResponse(response)
        }
    }

    suspend fun createChannel(guild: Guild?, data: DiscordChannel): Channel {
        val guildId = guild?.idSnowflake
        val deviousChannelData = DeviousChannelData.from(guildId, data)

        m.rpc.execute(PutChannelRequest(data.id, deviousChannelData))
        return Channel(m, guild, deviousChannelData)
    }

    suspend fun deleteChannel(guild: Guild?, channelId: Snowflake) {
        m.rpc.execute(DeleteChannelRequest(channelId))
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
}