package net.perfectdreams.loritta.cinnamon.discord.utils.entitycache

import dev.kord.common.entity.*
import kotlinx.serialization.*
import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.data.*
import net.perfectdreams.loritta.deviouscache.requests.GetGuildMemberRequest
import net.perfectdreams.loritta.deviouscache.requests.GetVoiceStateRequest
import net.perfectdreams.loritta.deviouscache.responses.GetGuildMemberResponse
import net.perfectdreams.loritta.deviouscache.responses.GetVoiceStateResponse
import net.perfectdreams.loritta.morenitta.LorittaBot
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
        val guild = loritta.deviousFun.getGuildById(guildId)

        return GuildEntities(
            guild?.roles?.map { it.role } ?: emptyList(),
            guild?.channels?.map { it.channel } ?: emptyList(),
            guild?.emotes?.map { it.emoji } ?: emptyList()
        )
    }

    /**
     * Gets the [member]'s roles in [guildId]
     */
    suspend fun getRoles(guildId: Snowflake, member: DiscordGuildMember) = getRoles(guildId, member.roles)

    /**
     * Gets role informations of the following [roleIds] in [guildId]
     */
    suspend fun getRoles(guildId: Snowflake, roleIds: Collection<Snowflake>): List<DeviousRoleData> {
        return loritta.deviousFun.getGuildById(guildId)?.roles?.map { it.role } ?: emptyList()
    }

    /**
     * Creates a [LazyCachedPermissions] class with the [guildId], [channelId] and with Loritta's user ID.
     *
     * It is lazy because the permission bitset is only retrieved after a [LazyCachedPermissions.hasPermission] check is triggered.
     *
     * It is cached because the permission bitset is cached, so after the permission bitset is already retrieved, it won't be retrieved again.
     */
    fun getLazyCachedLorittaPermissions(guildId: Snowflake): LazyCachedPermissions =
        GuildLazyCachedPermissions(rest, loritta, this, guildId, lorittaDiscordConfig.applicationId)

    /**
     * Creates a [LazyCachedPermissions] class with the [guildId], [channelId] and [userId].
     *
     * It is lazy because the permission bitset is only retrieved after a [LazyCachedPermissions.hasPermission] check is triggered.
     *
     * It is cached because the permission bitset is cached, so after the permission bitset is already retrieved, it won't be retrieved again.
     */
    fun getLazyCachedPermissions(guildId: Snowflake, userId: Snowflake): LazyCachedPermissions =
        GuildLazyCachedPermissions(rest, loritta, this, guildId, userId)

    /**
     * Creates a [LazyCachedPermissions] class with the [guildId], [channelId] and with Loritta's user ID.
     *
     * It is lazy because the permission bitset is only retrieved after a [LazyCachedPermissions.hasPermission] check is triggered.
     *
     * It is cached because the permission bitset is cached, so after the permission bitset is already retrieved, it won't be retrieved again.
     */
    fun getLazyCachedLorittaPermissions(guildId: Snowflake, channelId: Snowflake): LazyCachedPermissions =
        GuildChannelLazyCachedPermissions(rest, loritta, this, guildId, channelId, lorittaDiscordConfig.applicationId)

    /**
     * Creates a [LazyCachedPermissions] class with the [guildId], [channelId] and [userId].
     *
     * It is lazy because the permission bitset is only retrieved after a [LazyCachedPermissions.hasPermission] check is triggered.
     *
     * It is cached because the permission bitset is cached, so after the permission bitset is already retrieved, it won't be retrieved again.
     */
    fun getLazyCachedPermissions(guildId: Snowflake, channelId: Snowflake, userId: Snowflake): LazyCachedPermissions =
        GuildChannelLazyCachedPermissions(rest, loritta, this, guildId, channelId, userId)

    /**
     * Creates a [LazyCachedPermissions] class with the [guildId], [channelId] and [userId].
     *
     * It is lazy because the permission bitset is only retrieved after a [LazyCachedPermissions.hasPermission] check is triggered.
     *
     * It is cached because the permission bitset is cached, so after the permission bitset is already retrieved, it won't be retrieved again.
     */
    suspend fun lorittaHasPermission(guildId: Snowflake, channelId: Snowflake, vararg permissions: Permission) =
        getLazyCachedLorittaPermissions(guildId, channelId).hasPermission(*permissions)

    /**
     * Creates a [LazyCachedPermissions] class with the [guildId], [channelId] and [userId].
     *
     * It is lazy because the permission bitset is only retrieved after a [LazyCachedPermissions.hasPermission] check is triggered.
     *
     * It is cached because the permission bitset is cached, so after the permission bitset is already retrieved, it won't be retrieved again.
     */
    suspend fun hasPermission(
        guildId: Snowflake,
        channelId: Snowflake,
        userId: Snowflake,
        vararg permissions: Permission
    ) = getLazyCachedPermissions(guildId, channelId, userId).hasPermission(*permissions)

    /**
     * Gets Loritta's permissions in [channelId] on [guildId].
     *
     * @see getPermissions
     */
    suspend fun getLorittaPermissions(guildId: Snowflake, channelId: Snowflake) =
        getPermissions(guildId, channelId, lorittaDiscordConfig.applicationId)

    /**
     * Gets [userId]'s permissions on [guildId].
     *
     * @see getLorittaPermissions
     */
    suspend fun getPermissions(guildId: Snowflake, userId: Snowflake): GuildPermissionsResult {
        // Create an empty permissions object
        var permissions = Permissions()

        val memberData =
            (loritta.deviousFun.rpc.execute(GetGuildMemberRequest(guildId.toLightweightSnowflake(), userId.toLightweightSnowflake())) as? GetGuildMemberResponse)?.member
                ?: return GuildPermissionsResult(
                    // They aren't in the server, no need to continue then
                    permissions,
                    userNotInGuild = true,
                    missingRoles = false, // This is actually "Unknown"
                )

        val userRoleIds = memberData.roles

        val guild = loritta.deviousFun.getGuildById(guildId) ?: return GuildPermissionsResult(
            // The guild doesn't exist
            permissions,
            userNotInGuild = true,
            missingRoles = false, // This is actually "Unknown"
        )

        val userRoles = guild.roles.filter { it.idSnowflake.toLightweightSnowflake() in userRoleIds }

        var missingRoles = false

        // We are going to validate if there are any missing roles
        for (userRoleId in userRoleIds) {
            if (!userRoles.any { it.idSnowflake.toLightweightSnowflake() == userRoleId }) {
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
        entityIds.addAll(userRoleIds.map { it.toKordSnowflake() })

        return GuildPermissionsResult(
            permissions,
            userNotInGuild = false,
            missingRoles = missingRoles
        )
    }

    /**
     * Gets [userId]'s permissions in [channelId] on [guildId].
     *
     * @see getLorittaPermissions
     */
    suspend fun getPermissions(
        guildId: Snowflake,
        channelId: Snowflake,
        userId: Snowflake
    ): GuildChannelPermissionsResult {
        // Create an empty permissions object
        var permissions = Permissions()

        val memberData =
            (loritta.deviousFun.rpc.execute(GetGuildMemberRequest(guildId.toLightweightSnowflake(), userId.toLightweightSnowflake())) as? GetGuildMemberResponse)?.member
                ?: return GuildChannelPermissionsResult( // They aren't in the server, no need to continue then
                    permissions,
                    userNotInGuild = true,
                    missingRoles = false, // This is actually "Unknown"
                    missingChannels = false // This is actually "Unknown"
                )

        val userRoleIds = memberData.roles

        val guild =
            loritta.deviousFun.getGuildById(guildId) ?: return GuildChannelPermissionsResult( // The guild doesn't exist
                permissions,
                userNotInGuild = true,
                missingRoles = false, // This is actually "Unknown"
                missingChannels = false // This is actually "Unknown"
            )

        val userRoles = guild.roles.filter { it.idSnowflake.toLightweightSnowflake() in userRoleIds }

        var missingRoles = false
        var missingChannels = false

        val guildChannel = guild.channels.firstOrNull { it.idSnowflake == channelId }

        // We are going to validate if there are any missing roles
        for (userRoleId in userRoleIds) {
            if (!userRoles.any { it.idSnowflake.toLightweightSnowflake() == userRoleId }) {
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
        entityIds.addAll(userRoleIds.map { it.toKordSnowflake() })

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

        return GuildChannelPermissionsResult(
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
        return (loritta.deviousFun.rpc.execute(
            GetVoiceStateRequest(
                guildId.toLightweightSnowflake(),
                userId.toLightweightSnowflake()
            )
        ) as? GetVoiceStateResponse)?.channelId?.toKordSnowflake()
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