package net.perfectdreams.loritta.cinnamon.discord.utils.entitycache

import dev.kord.common.entity.DiscordChannel
import dev.kord.common.entity.DiscordEmoji
import dev.kord.common.entity.DiscordGuildMember
import dev.kord.common.entity.DiscordRole
import dev.kord.common.entity.OverwriteType
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.utils.redis.hsetIfMapNotEmpty
import net.perfectdreams.loritta.cinnamon.discord.utils.redis.toMap
import net.perfectdreams.loritta.cinnamon.discord.utils.redis.valueOrNull
import net.perfectdreams.loritta.cinnamon.pudding.utils.HashEncoder
import org.jetbrains.exposed.sql.*
import java.util.*

/**
 * Services related to Discord entity caching
 */
@OptIn(ExperimentalLettuceCoroutinesApi::class)
class DiscordCacheService(
    private val loritta: LorittaCinnamon
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val rest = loritta.rest
    private val lorittaDiscordConfig = loritta.discordConfig
    private val pudding = loritta.services
    private val redisCommands = loritta.redisCommands

    suspend fun getDiscordEntitiesOfGuild(guildId: Snowflake): GuildEntities {
        val roles = loritta.redisCommands.hgetall(loritta.redisKeys.discordGuildRoles(guildId))
            .toList(mutableListOf())
            .toMap()
            .mapNotNull {
                it.value.valueOrNull?.let { Json.decodeFromString<DiscordRole>(it) }
            }

        val channels = loritta.redisCommands.hgetall(loritta.redisKeys.discordGuildChannels(guildId))
            .toList(mutableListOf())
            .toMap()
            .mapNotNull {
                it.value.valueOrNull?.let { Json.decodeFromString<DiscordChannel>(it) }
            }

        val emojis = loritta.redisCommands.hgetall(loritta.redisKeys.discordGuildEmojis(guildId))
            .toList(mutableListOf())
            .toMap()
            .mapNotNull {
                it.value.valueOrNull?.let { Json.decodeFromString<DiscordEmoji>(it) }
            }

        return GuildEntities(
            roles,
            channels,
            emojis
        )
    }

    /**
     * Gets the [member]'s roles in [guildId]
     */
    suspend fun getRoles(guildId: Snowflake, member: DiscordGuildMember) = getRoles(guildId, member.roles)

    /**
     * Gets role informations of the following [roleIds] in [guildId]
     */
    suspend fun getRoles(guildId: Snowflake, roleIds: Collection<Snowflake>): List<DiscordRole> {
        val value = loritta.redisCommands.hget(loritta.redisKeys.discordGuilds(guildId), "roles")

        return value?.let { Json.decodeFromString<List<DiscordRole>>(it).let { it.filter { it.id in roleIds } } } ?: emptyList()
    }

    /**
     * Creates a [LazyCachedPermissions] class with the [guildId], [channelId] and with Loritta's user ID.
     *
     * It is lazy because the permission bitset is only retrieved after a [LazyCachedPermissions.hasPermission] check is triggered.
     *
     * It is cached because the permission bitset is cached, so after the permission bitset is already retrieved, it won't be retrieved again.
     */
    fun getLazyCachedLorittaPermissions(guildId: Snowflake, channelId: Snowflake) = LazyCachedPermissions(rest, pudding, this, guildId, channelId, Snowflake(lorittaDiscordConfig.applicationId))

    /**
     * Creates a [LazyCachedPermissions] class with the [guildId], [channelId] and [userId].
     *
     * It is lazy because the permission bitset is only retrieved after a [LazyCachedPermissions.hasPermission] check is triggered.
     *
     * It is cached because the permission bitset is cached, so after the permission bitset is already retrieved, it won't be retrieved again.
     */
    fun getLazyCachedPermissions(guildId: Snowflake, channelId: Snowflake, userId: Snowflake) = LazyCachedPermissions(rest, pudding, this, guildId, channelId, userId)

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
    suspend fun getLorittaPermissions(guildId: Snowflake, channelId: Snowflake) = getPermissions(guildId, channelId, Snowflake(lorittaDiscordConfig.applicationId))

    /**
     * Gets [userId]'s permissions in [channelId] on [guildId].
     *
     * @see getLorittaPermissions
     */
    suspend fun getPermissions(guildId: Snowflake, channelId: Snowflake, userId: Snowflake): PermissionsResult {
        // Create an empty permissions object
        var permissions = Permissions()

        val discordGuildMember = loritta.redisCommands
            .hget(loritta.redisKeys.discordGuildMembers(guildId), userId.toString())
            ?: return PermissionsResult( // They aren't in the server, no need to continue then
                permissions,
                userNotInGuild = true,
                missingRoles = false, // This is actually "Unknown"
                missingChannels = false // This is also "Unknown"
            )

        val userRoleIds = Json.decodeFromString<PuddingGuildMember>(discordGuildMember).roles

        val userRoles = userRoleIds.mapNotNull {
            loritta.redisCommands.hget(loritta.redisKeys.discordGuildRoles(guildId), it.toString())
        }.map { Json.decodeFromString<DiscordRole>(it) }

        var missingRoles = false
        var missingChannels = false

        val guildChannel = loritta.redisCommands.hget(loritta.redisKeys.discordGuildChannels(guildId), channelId.toString())
            ?.let { Json.decodeFromString<DiscordChannel>(it) }

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
        val permissionOverwrites = guildChannel?.permissionOverwrites?.value

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

        return PermissionsResult(
            permissions,
            userNotInGuild = false,
            missingRoles = missingRoles,
            missingChannels = missingChannels
        )
    }

    /**
     * Gets the [userId]'s voice channel ID on [guildId], if they are connected to a voice channel
     *
     * @param guildId the guild's ID
     * @param userId  the user's ID
     * @return the voice channel ID, if they are connected to a voice channel
     */
    suspend fun getUserConnectedVoiceChannel(guildId: Snowflake, userId: Snowflake): Snowflake? {
        return loritta.redisCommands.hget(loritta.redisKeys.discordGuildVoiceStates(guildId), userId.toString())
            ?.let { Json.decodeFromString<PuddingGuildVoiceState>(it) }
            ?.channelId
    }

    suspend fun createOrUpdateGuild(
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

        loritta.redisTransaction {
            // Delete all because we want to upsert everything
            del(loritta.redisKeys.discordGuildRoles(guildId))
            del(loritta.redisKeys.discordGuildChannels(guildId))
            del(loritta.redisKeys.discordGuildEmojis(guildId))

            hsetIfMapNotEmpty(
                loritta.redisKeys.discordGuildRoles(guildId),
                guildRoles.associate {
                    it.id.toString() to Json.encodeToString(it)
                }
            )

            if (guildChannels != null) {
                hsetIfMapNotEmpty(
                    loritta.redisKeys.discordGuildChannels(guildId),
                    guildChannels.associate {
                        it.id.toString() to Json.encodeToString(it)
                    }
                )
            }

            hsetIfMapNotEmpty(
                loritta.redisKeys.discordGuildEmojis(guildId),
                guildEmojis.associate {
                    // Guild Emojis always have an ID
                    it.id!!.toString() to Json.encodeToString(it)
                }
            )
        }
    }

    suspend fun updateGuildEmojis(guildId: Snowflake, guildEmojis: List<DiscordEmoji>) {
        redisCommands.hset(
            loritta.redisKeys.discordGuilds(guildId),
            mapOf("emojis" to Json.encodeToString(guildEmojis))
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

    data class PermissionsResult(
        val permissions: Permissions,
        val userNotInGuild: Boolean,
        val missingRoles: Boolean,
        val missingChannels: Boolean
    )

    data class GuildEntities(
        val roles: List<DiscordRole>,
        val channels: List<DiscordChannel>,
        val emojis: List<DiscordEmoji>
    )
}