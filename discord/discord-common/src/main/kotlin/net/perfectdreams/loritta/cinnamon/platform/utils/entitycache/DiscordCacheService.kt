package net.perfectdreams.loritta.cinnamon.platform.utils.entitycache

import dev.kord.common.entity.DiscordChannel
import dev.kord.common.entity.DiscordEmoji
import dev.kord.common.entity.DiscordGuildMember
import dev.kord.common.entity.DiscordRole
import dev.kord.common.entity.OverwriteType
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.platform.utils.PuddingDiscordRolesList
import net.perfectdreams.loritta.cinnamon.platform.utils.config.LorittaDiscordConfig
import net.perfectdreams.loritta.cinnamon.platform.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.*
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

/**
 * Services related to Discord entity caching
 */
class DiscordCacheService(
    internal val lorittaDiscordConfig: LorittaDiscordConfig,
    private val pudding: Pudding
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun getDiscordEntitiesOfGuild(guildId: Snowflake): GuildEntities {
        return pudding.transaction {
            val roles = DiscordRoles.slice(DiscordRoles.data)
                .select { DiscordRoles.guild eq guildId.toLong() }
                .map { Json.decodeFromString<DiscordRole>(it[DiscordRoles.data]) }

            val channels = DiscordChannels.slice(DiscordChannels.data)
                .select { DiscordChannels.guild eq guildId.toLong() }
                .map { Json.decodeFromString<DiscordChannel>(it[DiscordChannels.data]) }

            val emojis = DiscordEmojis.slice(DiscordEmojis.data)
                .select { DiscordEmojis.guild eq guildId.toLong() }
                .map { Json.decodeFromString<DiscordEmoji>(it[DiscordEmojis.data]) }

            return@transaction GuildEntities(
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
    suspend fun getRoles(guildId: Snowflake, roleIds: List<Snowflake>): List<DiscordRole> {
        return pudding.transaction {
            DiscordRoles.slice(DiscordRoles.data)
                .select {
                    DiscordRoles.guild eq guildId.toLong() and (DiscordRoles.role inList roleIds.map { it.toLong() })
                }.map { Json.decodeFromString(it[DiscordRoles.data]) }
        }
    }

    /**
     * Creates a [LazyCachedPermissions] class with the [guildId], [channelId] and with Loritta's user ID.
     *
     * It is lazy because the permission bitset is only retrieved after a [LazyCachedPermissions.hasPermission] check is triggered.
     *
     * It is cached because the permission bitset is cached, so after the permission bitset is already retrieved, it won't be retrieved again.
     */
    fun getLazyCachedLorittaPermissions(guildId: Snowflake, channelId: Snowflake) = LazyCachedPermissions(this, guildId, channelId, Snowflake(lorittaDiscordConfig.applicationId))

    /**
     * Creates a [LazyCachedPermissions] class with the [guildId], [channelId] and [userId].
     *
     * It is lazy because the permission bitset is only retrieved after a [LazyCachedPermissions.hasPermission] check is triggered.
     *
     * It is cached because the permission bitset is cached, so after the permission bitset is already retrieved, it won't be retrieved again.
     */
    fun getLazyCachedPermissions(guildId: Snowflake, channelId: Snowflake, userId: Snowflake) = LazyCachedPermissions(this, guildId, channelId, userId)

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
    suspend fun getPermissions(guildId: Snowflake, channelId: Snowflake, userId: Snowflake): Permissions {
        // Create an empty permissions object
        var permissions = Permissions()

        pudding.transaction {
            val userRoleIds = (
                    DiscordGuildMembers
                        .slice(DiscordGuildMembers.roles)
                        .selectFirstOrNull { DiscordGuildMembers.guildId eq guildId.toLong() and (DiscordGuildMembers.userId eq userId.toLong()) }
                        ?.get(DiscordGuildMembers.roles)
                        ?.let {
                            Json.decodeFromString<PuddingDiscordRolesList>(it)
                        } ?: emptyList()
                    ) + guildId.toString() // Because the user always have the "@everyone" role!

            val userRoles = DiscordRoles.slice(DiscordRoles.data).select {
                DiscordRoles.guild eq guildId.toLong() and (DiscordRoles.role inList userRoleIds.map { it.toLong() })
            }.map { Json.decodeFromString<DiscordRole>(it[DiscordRoles.data]) }

            val guildChannel = DiscordChannels.selectFirstOrNull {
                DiscordChannels.guild eq guildId.toLong() and (DiscordChannels.channel eq channelId.toLong())
            }?.let { Json.decodeFromString<DiscordChannel>(it[DiscordChannels.data]) }

            // We are going to validate if there are any missing roles
            for (userRoleId in userRoleIds) {
                if (!userRoles.any { it.id.toString() == userRoleId }) {
                    logger.warn { "Missing role $userRoleId in $guildId! We will pretend that it doesn't exist and hope for the best..." }
                }
            }

            // And also validate if the channel is null
            if (guildChannel != null) {
                logger.warn { "Missing channel $channelId in $guildId! We will pretend that it doesn't exist and hope for the best..." }
            }

            // The order of the roles doesn't matter!
            userRoles
                .forEach {
                    // Keep "plus"'ing the permissions!
                    permissions = permissions.plus(it.permissions)
                }

            val entityIds = mutableSetOf(userId)
            entityIds.addAll(userRoleIds.map { Snowflake(it) })

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
        }

        return permissions
    }

    /**
     * Gets the [userId]'s voice channel ID on [guildId], if they are connected to a voice channel
     *
     * @param guildId the guild's ID
     * @param userId  the user's ID
     * @return the voice channel ID, if they are connected to a voice channel
     */
    suspend fun getUserConnectedVoiceChannel(guildId: Snowflake, userId: Snowflake): Snowflake? {
        return pudding.transaction {
            DiscordVoiceStates.slice(DiscordVoiceStates.channel).selectFirstOrNull {
                DiscordVoiceStates.guild eq guildId.toLong() and (DiscordVoiceStates.user eq userId.toLong())
            }?.get(DiscordVoiceStates.channel)?.let { Snowflake(it) }
        }
    }
    data class GuildEntities(
        val roles: List<DiscordRole>,
        val channels: List<DiscordChannel>,
        val emojis: List<DiscordEmoji>
    )
}