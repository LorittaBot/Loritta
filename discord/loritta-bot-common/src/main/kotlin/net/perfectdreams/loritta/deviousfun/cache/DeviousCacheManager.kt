package net.perfectdreams.loritta.deviousfun.cache

import dev.kord.common.entity.*
import dev.kord.common.entity.optional.value
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.discord.utils.entitycache.PuddingGuildVoiceState
import net.perfectdreams.loritta.cinnamon.discord.utils.redis.hgetByteArray
import net.perfectdreams.loritta.cinnamon.discord.utils.redis.hsetByteArray
import net.perfectdreams.loritta.cinnamon.discord.utils.redis.hsetByteArrayOrDelIfMapIsEmpty
import net.perfectdreams.loritta.deviousfun.JDA
import net.perfectdreams.loritta.deviousfun.entities.*
import net.perfectdreams.loritta.morenitta.cache.decode
import net.perfectdreams.loritta.morenitta.cache.encode
import redis.clients.jedis.Transaction

/**
 * Manages cache
 */
class DeviousCacheManager(val m: JDA) {
    private val binaryCacheTransformers = m.loritta.binaryCacheTransformers

    suspend fun getGuild(id: Snowflake): Guild? {
        val cachedGuildData = m.loritta.redisConnection {
            it.hget(m.loritta.redisKeys.discordGuilds(), id.toString())
        } ?: return null

        val (roles, channels, emojis) = m.loritta.cache.getDiscordEntitiesOfGuild(id)

        val guildData = Json.decodeFromString<DeviousGuildData>(cachedGuildData)

        val cacheWrapper = Guild.CacheWrapper()
        val guild = Guild(
            m,
            guildData,
            cacheWrapper
        )

        for (data in roles) {
            cacheWrapper.roles[data.id] = Role(
                m,
                guild,
                data
            )
        }

        for (data in channels) {
            cacheWrapper.channels[data.id] = Channel(
                m,
                guild,
                data
            )
        }

        for (data in emojis) {
            cacheWrapper.emotes[data.id] = DiscordGuildEmote(
                m,
                guild,
                data
            )
        }

        return guild
    }

    suspend fun createGuild(
        data: DiscordGuild,
        guildChannels: List<DiscordChannel>?,
    ): Guild {
        val deviousGuildData = DeviousGuildData(
            data.id,
            data.name,
            data.ownerId,
            data.icon,
            data.vanityUrlCode,
            data.premiumSubscriptionCount.value ?: 0,
            data.memberCount.value ?: data.approximateMemberCount.value ?: 0,
            data.splash.value,
        )
        val guildMembers = data.members.value ?: emptyList()
        val guildVoiceStates = data.voiceStates.value ?: emptyList()

        val channelsOfThisGuild = if (guildChannels != null) {
            m.loritta.redisConnection {
                it.smembers(m.loritta.redisKeys.discordGuildChannels(data.id))
            }
        } else null

        val rolesData = data.roles.map { DeviousRoleData.from(it) }

        val emojisData = convertStuff(data.emojis)

        m.loritta.redisTransaction {
            it.hset(m.loritta.redisKeys.discordGuilds(), data.id.toString(), Json.encodeToString(deviousGuildData))

            // Upsert roles
            it.hsetByteArray(
                m.loritta.redisKeys.discordGuildRoles(deviousGuildData.id),
                rolesData.map {
                    it.id.toString() to m.loritta.binaryCacheTransformers.roles.encode(it)
                }.toMap()
            )

            if (guildChannels != null) {
                // Upsert guild channel set
                // We will use a set indicating what channels are in this guild
                it.del(m.loritta.redisKeys.discordGuildChannels(deviousGuildData.id))
                it.sadd(
                    m.loritta.redisKeys.discordGuildChannels(deviousGuildData.id),
                    *data.channels.value!!.map { it.id.toString() }.toTypedArray()
                )

                // Delete all channels related to this guild
                if (channelsOfThisGuild != null)
                    it.hdel(m.loritta.redisKeys.discordChannels(), *channelsOfThisGuild.toTypedArray())

                // Upsert channels
                it.hsetByteArray(
                    m.loritta.redisKeys.discordChannels(),
                    guildChannels.map {
                        it.id.toString() to binaryCacheTransformers.channels.encode(
                            DeviousChannelData.from(
                                data.id,
                                it
                            )
                        )
                    }.toMap()
                )
            }

            // Upsert emojis
            storeEmojis(
                it,
                data.id,
                emojisData
            )

            // Insert members
            for (member in guildMembers) {
                it.hsetByteArray(
                    m.loritta.redisKeys.discordGuildMembers(data.id),
                    member.user.value!!.id.toString(),
                    binaryCacheTransformers.members.encode(
                        DeviousMemberData.from(member)
                    )
                )
            }

            // Insert voice states
            it.hsetByteArrayOrDelIfMapIsEmpty(
                m.loritta.redisKeys.discordGuildVoiceStates(data.id),
                guildVoiceStates.associate {
                    it.userId.toString() to binaryCacheTransformers.voiceStates.encode(
                        PuddingGuildVoiceState(
                            it.channelId!!, // Shouldn't be null because they are in a channel
                            it.userId
                        )
                    )
                }
            )
        }

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

    suspend fun deleteGuild(guildId: Snowflake) {
        val channelsOfThisGuild = m.loritta.redisConnection {
            it.smembers(m.loritta.redisKeys.discordGuildChannels(guildId))
        }

        m.loritta.redisTransaction {
            it.hdel(m.loritta.redisKeys.discordGuilds(), guildId.toString())
            it.del(m.loritta.redisKeys.discordGuildMembers(guildId))
            it.del(m.loritta.redisKeys.discordGuildRoles(guildId))
            it.del(m.loritta.redisKeys.discordGuildChannels(guildId))
            it.del(m.loritta.redisKeys.discordGuildEmojis(guildId))
            it.del(m.loritta.redisKeys.discordGuildVoiceStates(guildId))
            it.hdel(m.loritta.redisKeys.discordChannels(), *channelsOfThisGuild.toTypedArray())
        }
    }

    fun convertStuff(emojis: List<DiscordEmoji>): List<DeviousGuildEmojiData> {
        return emojis.map { DeviousGuildEmojiData.from(it) }
    }

    fun storeEmojis(transaction: Transaction, guildId: Snowflake, emojis: List<DeviousGuildEmojiData>) {
        // Upsert emojis
        transaction.hsetByteArray(
            m.loritta.redisKeys.discordGuildEmojis(guildId),
            emojis.map {
                it.id.toString() to binaryCacheTransformers.emojis.encode(it)
            }.toMap()
        )
    }

    suspend fun getUser(id: Snowflake): User? {
        val data = m.loritta.redisConnection {
            it.hgetByteArray(
                m.loritta.redisKeys.discordUsers(),
                id.toString()
            )
        } ?: return null

        return User(
            m,
            id,
            binaryCacheTransformers.users.decode(data)
        )
    }

    suspend fun createUser(user: DiscordUser, addToCache: Boolean): User {
        val deviousUserData = DeviousUserData.from(user)

        // Let's only cache it if it isn't a webhook
        if (addToCache) {
            println("Storing user ${user.id} on cache")
            m.loritta.redisConnection {
                it.hsetByteArray(
                    m.loritta.redisKeys.discordUsers(),
                    user.id.toString(),
                    binaryCacheTransformers.users.encode(deviousUserData)
                )
            }
        }

        return User(m, user.id, deviousUserData)
    }

    suspend fun getMember(user: User, guild: Guild): Member? {
        val data = m.loritta.redisConnection {
            it.hgetByteArray(
                m.loritta.redisKeys.discordGuildMembers(guild.idSnowflake),
                user.id
            )
        } ?: return null

        return Member(
            m,
            binaryCacheTransformers.members.decode(data),
            guild,
            user
        )
    }

    suspend fun createMember(user: User, guild: Guild, member: DiscordGuildMember)
            = createMember(user, guild, DeviousMemberData.from(member))

    suspend fun createMember(user: User, guild: Guild, member: DiscordAddedGuildMember)
            = createMember(user, guild, DeviousMemberData.from(member))

    suspend fun createMember(user: User, guild: Guild, member: DiscordUpdatedGuildMember): Member {
        // Because the DiscordUpdatedGuildMember entity does not have some fields, we will use them as a copy
        val oldDeviousMemberData = getMember(user, guild)?.member
        return createMember(user, guild, DeviousMemberData.from(member, oldDeviousMemberData))
    }

    suspend fun createMember(user: User, guild: Guild, deviousMemberData: DeviousMemberData): Member {
        m.loritta.redisConnection {
            it.hsetByteArray(
                m.loritta.redisKeys.discordGuildMembers(guild.idSnowflake),
                user.id,
                binaryCacheTransformers.members.encode(deviousMemberData)
            )
        }

        return Member(
            m,
            deviousMemberData,
            guild,
            user
        )
    }

    suspend fun deleteMember(guild: Guild, userId: Snowflake) {
        m.loritta.redisConnection {
            // It seems that deleting a role does trigger a member update related to the role removal, so we won't need to manually remove it (yay)
            it.hdel(
                m.loritta.redisKeys.discordGuildMembers(guild.idSnowflake),
                userId.toString()
            )
        }
    }

    suspend fun createRole(guild: Guild, role: DiscordRole): Role {
        val data = DeviousRoleData.from(role)

        m.loritta.redisConnection {
            it.hsetByteArray(
                m.loritta.redisKeys.discordGuildRoles(guild.idSnowflake),
                data.id.toString(),
                binaryCacheTransformers.roles.encode(data)
            )
        }

        return Role(
            m,
            guild,
            data
        )
    }

    suspend fun deleteRole(guild: Guild, roleId: Snowflake) {
        m.loritta.redisConnection {
            // It seems that deleting a role does trigger a member update related to the role removal, so we won't need to manually remove it (yay)
            it.hdel(
                m.loritta.redisKeys.discordGuildRoles(guild.idSnowflake),
                roleId.toString()
            )
        }
    }

    suspend fun getChannel(channelId: Snowflake): Channel? {
        val data = m.loritta.redisConnection {
            it.hgetByteArray(
                m.loritta.redisKeys.discordChannels(),
                channelId.toString()
            )
        }?.let { binaryCacheTransformers.channels.decode(it) } ?: return null

        println("getChannel $channelId, guildId is ${data.guildId}")
        val guildData = data.guildId?.let { getGuild(it) ?: return null }

        return Channel(m, guildData, data)
    }

    suspend fun createChannel(guild: Guild?, data: DiscordChannel): Channel {
        val guildId = guild?.idSnowflake
        println("Creating channel $data on guild ${guildId}")
        val deviousChannelData = DeviousChannelData.from(guildId, data)

        m.loritta.redisTransaction {
            it.hsetByteArray(
                m.loritta.redisKeys.discordChannels(),
                deviousChannelData.id.toString(),
                binaryCacheTransformers.channels.encode(deviousChannelData)
            )

            if (guildId != null) {
                // Add the channel to the guild's channel set
                it.sadd(
                    m.loritta.redisKeys.discordGuildChannels(guildId),
                    data.id.toString()
                )
            }
        }

        return Channel(m, guild, deviousChannelData)
    }

    suspend fun deleteChannel(guild: Guild?, channelId: Snowflake) {
        val guildId = guild?.idSnowflake

        m.loritta.redisTransaction {
            it.hdel(
                m.loritta.redisKeys.discordChannels(),
                channelId.toString()
            )

            if (guildId != null) {
                // Add the channel to the guild's channel set
                it.srem(
                    m.loritta.redisKeys.discordGuildChannels(guildId),
                    channelId.toString()
                )
            }
        }
    }
}