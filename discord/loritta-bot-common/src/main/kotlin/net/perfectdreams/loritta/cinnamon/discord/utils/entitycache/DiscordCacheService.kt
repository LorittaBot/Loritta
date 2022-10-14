package net.perfectdreams.loritta.cinnamon.discord.utils.entitycache

import dev.kord.common.entity.DiscordChannel
import dev.kord.common.entity.DiscordEmoji
import dev.kord.common.entity.DiscordGuildMember
import dev.kord.common.entity.DiscordRole
import dev.kord.common.entity.OverwriteType
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.*
import mu.KotlinLogging
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.utils.redis.hgetAllByteArray
import net.perfectdreams.loritta.cinnamon.discord.utils.redis.hgetByteArray
import net.perfectdreams.loritta.cinnamon.discord.utils.redis.hsetByteArrayOrDelIfMapIsEmpty
import net.perfectdreams.loritta.cinnamon.pudding.utils.HashEncoder
import net.perfectdreams.loritta.deviousfun.cache.DeviousChannelData
import net.perfectdreams.loritta.deviousfun.cache.DeviousGuildEmojiData
import net.perfectdreams.loritta.deviousfun.cache.DeviousRoleData
import net.perfectdreams.loritta.morenitta.cache.decode
import net.perfectdreams.loritta.morenitta.cache.encode
import org.jetbrains.exposed.sql.*
import java.util.*

/**
 * Services related to Discord entity caching
 */
class DiscordCacheService(
    private val loritta: LorittaBot
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val rest = loritta.rest
    private val lorittaDiscordConfig = loritta.config.loritta.discord
    private val pudding = loritta.pudding

    suspend fun getDiscordEntitiesOfGuild(guildId: Snowflake): GuildEntities {
        return loritta.redisConnection("get discord entities of guild $guildId") {
            val roles = it.hgetAllByteArray(loritta.redisKeys.discordGuildRoles(guildId))
                .values
                .map {
                    loritta.binaryCacheTransformers.roles.decode(it)
                }

            val channelIds = it.smembers(loritta.redisKeys.discordGuildChannels(guildId))
            val channels = it.hmget(loritta.redisKeys.discordChannels().toByteArray(Charsets.UTF_8), *channelIds.map { it.toByteArray(Charsets.UTF_8) }.toTypedArray())
                .filterNotNull()
                .map {
                    loritta.binaryCacheTransformers.channels.decode(it)
                }

            val emojis = it.hgetAllByteArray(loritta.redisKeys.discordGuildEmojis(guildId))
                .values
                .map {
                    loritta.binaryCacheTransformers.emojis.decode(it)
                }

            return@redisConnection GuildEntities(
                roles,
                channels,
                emojis
            )
        }
    }

    /**
     * Gets the [member]'s roles in [guildId]
     */
    suspend fun getRoles(guildId: Snowflake, member: DiscordGuildMember) = getRoles(guildId, member.roles)

    /**
     * Gets role informations of the following [roleIds] in [guildId]
     */
    suspend fun getRoles(guildId: Snowflake, roleIds: Collection<Snowflake>): List<DeviousRoleData> {
        return loritta.redisConnection("get roles of guild $guildId") {
            it.hgetAllByteArray(loritta.redisKeys.discordGuildRoles(guildId))
                .filterKeys { Snowflake(it.toLong()) in roleIds }
                .map {
                    loritta.binaryCacheTransformers.roles.decode(it.value)
                }
        }
    }

    /**
     * Creates a [LazyCachedPermissions] class with the [guildId], [channelId] and with Loritta's user ID.
     *
     * It is lazy because the permission bitset is only retrieved after a [LazyCachedPermissions.hasPermission] check is triggered.
     *
     * It is cached because the permission bitset is cached, so after the permission bitset is already retrieved, it won't be retrieved again.
     */
    fun getLazyCachedLorittaPermissions(guildId: Snowflake): LazyCachedPermissions = GuildLazyCachedPermissions(rest, loritta, this, guildId, lorittaDiscordConfig.applicationId)

    /**
     * Creates a [LazyCachedPermissions] class with the [guildId], [channelId] and [userId].
     *
     * It is lazy because the permission bitset is only retrieved after a [LazyCachedPermissions.hasPermission] check is triggered.
     *
     * It is cached because the permission bitset is cached, so after the permission bitset is already retrieved, it won't be retrieved again.
     */
    fun getLazyCachedPermissions(guildId: Snowflake, userId: Snowflake): LazyCachedPermissions = GuildLazyCachedPermissions(rest, loritta, this, guildId, userId)

    /**
     * Creates a [LazyCachedPermissions] class with the [guildId], [channelId] and with Loritta's user ID.
     *
     * It is lazy because the permission bitset is only retrieved after a [LazyCachedPermissions.hasPermission] check is triggered.
     *
     * It is cached because the permission bitset is cached, so after the permission bitset is already retrieved, it won't be retrieved again.
     */
    fun getLazyCachedLorittaPermissions(guildId: Snowflake, channelId: Snowflake): LazyCachedPermissions = GuildChannelLazyCachedPermissions(rest, loritta, this, guildId, channelId, lorittaDiscordConfig.applicationId)

    /**
     * Creates a [LazyCachedPermissions] class with the [guildId], [channelId] and [userId].
     *
     * It is lazy because the permission bitset is only retrieved after a [LazyCachedPermissions.hasPermission] check is triggered.
     *
     * It is cached because the permission bitset is cached, so after the permission bitset is already retrieved, it won't be retrieved again.
     */
    fun getLazyCachedPermissions(guildId: Snowflake, channelId: Snowflake, userId: Snowflake): LazyCachedPermissions = GuildChannelLazyCachedPermissions(rest, loritta, this, guildId, channelId, userId)

    /**
     * Creates a [LazyCachedPermissions] class with the [guildId], [channelId] and [userId].
     *
     * It is lazy because the permission bitset is only retrieved after a [LazyCachedPermissions.hasPermission] check is triggered.
     *
     * It is cached because the permission bitset is cached, so after the permission bitset is already retrieved, it won't be retrieved again.
     */
    suspend fun lorittaHasPermission(guildId: Snowflake, channelId: Snowflake, vararg permissions: Permission) = getLazyCachedLorittaPermissions(guildId, channelId).hasPermission(*permissions)

    /**
     * Creates a [LazyCachedPermissions] class with the [guildId], [channelId] and [userId].
     *
     * It is lazy because the permission bitset is only retrieved after a [LazyCachedPermissions.hasPermission] check is triggered.
     *
     * It is cached because the permission bitset is cached, so after the permission bitset is already retrieved, it won't be retrieved again.
     */
    suspend fun hasPermission(guildId: Snowflake, channelId: Snowflake, userId: Snowflake, vararg permissions: Permission) = getLazyCachedPermissions(guildId, channelId, userId).hasPermission(*permissions)

    /**
     * Gets Loritta's permissions in [channelId] on [guildId].
     *
     * @see getPermissions
     */
    suspend fun getLorittaPermissions(guildId: Snowflake, channelId: Snowflake) = getPermissions(guildId, channelId, lorittaDiscordConfig.applicationId)

    /**
     * Gets [userId]'s permissions on [guildId].
     *
     * @see getLorittaPermissions
     */
    suspend fun getPermissions(guildId: Snowflake, userId: Snowflake): GuildPermissionsResult {
        // Create an empty permissions object
        var permissions = Permissions()

        return loritta.redisConnection("get user $userId permissions in guild $guildId") {
            val discordGuildMember = it
                .hgetByteArray(loritta.redisKeys.discordGuildMembers(guildId), userId.toString())
                ?: return@redisConnection GuildPermissionsResult( // They aren't in the server, no need to continue then
                    permissions,
                    userNotInGuild = true,
                    missingRoles = false, // This is actually "Unknown"
                )

            val userRoleIds = loritta.binaryCacheTransformers.members.decode(discordGuildMember).roles

            val userRoles = userRoleIds.mapNotNull { snowflake ->
                it.hgetByteArray(loritta.redisKeys.discordGuildRoles(guildId), snowflake.toString())
            }.map { loritta.binaryCacheTransformers.roles.decode(it) }

            var missingRoles = false

            // We are going to validate if there are any missing roles
            for (userRoleId in userRoleIds) {
                if (!userRoles.any { it.id == userRoleId }) {
                    logger.warn { "Missing role $userRoleId in $guildId! We will pretend that it doesn't exist and hope for the best..." }
                    missingRoles = true
                }
            }

            // The order of the roles doesn't matter!
            userRoles
                .forEach {
                    // Keep "plus"'ing the permissions!
                    permissions = permissions.plus(it.permissions)
                }

            val entityIds = mutableSetOf(userId)
            entityIds.addAll(userRoleIds.map { it })

            return@redisConnection GuildPermissionsResult(
                permissions,
                userNotInGuild = false,
                missingRoles = missingRoles
            )
        }
    }

    /**
     * Gets [userId]'s permissions in [channelId] on [guildId].
     *
     * @see getLorittaPermissions
     */
    suspend fun getPermissions(guildId: Snowflake, channelId: Snowflake, userId: Snowflake): GuildChannelPermissionsResult {
        // Create an empty permissions object
        var permissions = Permissions()

        return loritta.redisConnection("get user $userId permissions in $channelId in $guildId") {
            val discordGuildMember = it
                .hgetByteArray(loritta.redisKeys.discordGuildMembers(guildId), userId.toString())
                ?: return@redisConnection GuildChannelPermissionsResult( // They aren't in the server, no need to continue then
                    permissions,
                    userNotInGuild = true,
                    missingRoles = false, // This is actually "Unknown"
                    missingChannels = false // This is also "Unknown"
                )

            val userRoleIds = loritta.binaryCacheTransformers.members.decode(discordGuildMember).roles

            val userRoles = userRoleIds.mapNotNull { snowflake ->
                it.hgetByteArray(loritta.redisKeys.discordGuildRoles(guildId), snowflake.toString())
            }.map { loritta.binaryCacheTransformers.roles.decode(it) }

            var missingRoles = false
            var missingChannels = false

            val guildChannel = it.hgetByteArray(loritta.redisKeys.discordChannels(), channelId.toString())
                ?.let {
                    loritta.binaryCacheTransformers.channels.decode(it)
                }

            // We are going to validate if there are any missing roles
            for (userRoleId in userRoleIds) {
                if (!userRoles.any { it.id == userRoleId }) {
                    logger.warn { "Missing role $userRoleId in $guildId! We will pretend that it doesn't exist and hope for the best..." }
                    missingRoles = true
                }
            }

            // And also validate if the channel is null
            if (guildChannel == null) {
                logger.warn { "Missing channel $channelId in $guildId! We will pretend that it doesn't exist and hope for the best..." }
                missingChannels = true
            }

            // The order of the roles doesn't matter!
            userRoles
                .forEach {
                    // Keep "plus"'ing the permissions!
                    permissions = permissions.plus(it.permissions)
                }

            val entityIds = mutableSetOf(userId)
            entityIds.addAll(userRoleIds.map { it })

            // Now we will get permission overwrites
            val permissionOverwrites = guildChannel?.permissionOverwrites

            // https://discord.com/developers/docs/topics/permissions#permission-overwrites
            if (permissionOverwrites != null) {
                // First, the "@everyone" role permission overwrite
                val everyonePermissionOverwrite = permissionOverwrites.firstOrNull { it.id == guildId }
                if (everyonePermissionOverwrite != null) {
                    permissions = permissions.minus(everyonePermissionOverwrite.deny)
                    permissions = permissions.plus(everyonePermissionOverwrite.allow)
                }

                // Then, permission overwrites for specific roles
                val rolePermissionOverwrites = permissionOverwrites.filter {
                    it.type == OverwriteType.Role
                }.filter { it.id in entityIds }

                for (permissionOverwrite in rolePermissionOverwrites) {
                    permissions = permissions.minus(permissionOverwrite.deny)
                    permissions = permissions.plus(permissionOverwrite.allow)
                }

                // And finally, permission overwrites for specific members
                val memberPermissionOverwrites = permissionOverwrites.filter {
                    it.type == OverwriteType.Member
                }.filter { it.id in entityIds }

                for (permissionOverwrite in memberPermissionOverwrites) {
                    permissions = permissions.minus(permissionOverwrite.deny)
                    permissions = permissions.plus(permissionOverwrite.allow)
                }
            }

            return@redisConnection GuildChannelPermissionsResult(
                permissions,
                userNotInGuild = false,
                missingRoles = missingRoles,
                missingChannels = missingChannels
            )
        }
    }

    /**
     * Gets the [userId]'s voice channel ID on [guildId], if they are connected to a voice channel
     *
     * @param guildId the guild's ID
     * @param userId  the user's ID
     * @return the voice channel ID, if they are connected to a voice channel
     */
    suspend fun getUserConnectedVoiceChannel(guildId: Snowflake, userId: Snowflake): Snowflake? {
        return loritta.redisConnection("get voice channel of $userId in $guildId") {
            it.hgetByteArray(loritta.redisKeys.discordGuildVoiceStates(guildId), userId.toString())
                ?.let { loritta.binaryCacheTransformers.voiceStates.decode(it) }
                ?.channelId
        }
    }

    fun createOrUpdateGuild(
        redisTransaction: redis.clients.jedis.Transaction,
        guildId: Snowflake,
        guildName: String,
        guildIcon: String?,
        guildOwnerId: Snowflake,
        guildRoles: List<DiscordRole>,
        guildChannels: List<DiscordChannel>?,
        guildEmojis: List<DiscordEmoji>
    ) {
        // TODO: Where to store general info about the guild?
        // Does not exist, let's insert it
        /* DiscordGuilds.upsert(DiscordGuilds.id) {
            it[DiscordGuilds.id] = guildIdAsLong
            it[DiscordGuilds.name] = guildName
            it[DiscordGuilds.icon] = guildIcon
            it[DiscordGuilds.ownerId] = guildOwnerId.toLong()
        } */

        // Delete all because we want to upsert everything
        redisTransaction.hsetByteArrayOrDelIfMapIsEmpty(
            loritta.redisKeys.discordGuildRoles(guildId),
            guildRoles.associate {
                it.id.toString() to loritta.binaryCacheTransformers.roles.encode(DeviousRoleData.from(it))
            }
        )

        if (guildChannels != null) {
            redisTransaction.hsetByteArrayOrDelIfMapIsEmpty(
                loritta.redisKeys.discordGuildChannels(guildId),
                guildChannels.associate {
                    it.id.toString() to loritta.binaryCacheTransformers.channels.encode(DeviousChannelData.from(guildId, it))
                }
            )
        }

        redisTransaction.hsetByteArrayOrDelIfMapIsEmpty(
            loritta.redisKeys.discordGuildEmojis(guildId),
            guildEmojis.associate {
                // Guild Emojis always have an ID
                it.id!!.toString() to loritta.binaryCacheTransformers.emojis.encode(DeviousGuildEmojiData.from(it))
            }
        )
    }

    /**
     * Hashes [value]'s primitives with [Objects.hash] to create a hash that identifies the object.
     */
    inline fun <reified T> hashEntity(value: T): Int {
        // We use our own custom hash encoder because ProtoBuf can't encode the "Optional" fields, because it can't serialize null values
        // on a field that isn't marked as null
        val encoder = HashEncoder()
        encoder.encodeSerializableValue(serializer(), value)
        return Objects.hash(*encoder.list.toTypedArray())
    }

    // https://stackoverflow.com/a/58310635/7271796
    fun <T> Collection<T>.containsSameElements(other: Collection<T>): Boolean {
        // check collections aren't same
        if (this !== other) {
            // fast check of sizes
            if (this.size != other.size) return false

            // check other contains next element from this
            // all "it" must be in "other", if there isn't, then it should return false
            // (Kotlin fast fails in the "all" check!)
            return this.all { it in other }
        }
        // collections are same or they contain the same elements
        return true
    }

    data class GuildPermissionsResult(
        val permissions: Permissions,
        val userNotInGuild: Boolean,
        val missingRoles: Boolean
    )

    data class GuildChannelPermissionsResult(
        val permissions: Permissions,
        val userNotInGuild: Boolean,
        val missingRoles: Boolean,
        val missingChannels: Boolean
    )

    data class GuildEntities(
        val roles: List<DeviousRoleData>,
        val channels: List<DeviousChannelData>,
        val emojis: List<DeviousGuildEmojiData>
    )

    @Serializable
    data class LorittaCompressionHeader(
        val version: Int,
        val dictionaryId: ZstdDictionaries.Dictionary
    )
}