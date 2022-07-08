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
import net.perfectdreams.loritta.cinnamon.platform.utils.PuddingDiscordChannelsMap
import net.perfectdreams.loritta.cinnamon.platform.utils.PuddingDiscordEmojisMap
import net.perfectdreams.loritta.cinnamon.platform.utils.PuddingDiscordRolesList
import net.perfectdreams.loritta.cinnamon.platform.utils.PuddingDiscordRolesMap
import net.perfectdreams.loritta.cinnamon.platform.utils.config.LorittaDiscordConfig
import net.perfectdreams.loritta.cinnamon.platform.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.DiscordGuildMembers
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.DiscordGuilds
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull
import org.jetbrains.exposed.sql.and

/**
 * Services related to Discord entity caching
 */
class DiscordCacheService(
    internal val lorittaDiscordConfig: LorittaDiscordConfig,
    private val pudding: Pudding
) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val EMPTY_GUILD_ENTITIES = GuildEntities(emptyList(), emptyList(), emptyList())
    }

    suspend fun getDiscordEntitiesOfGuild(guildId: Snowflake): GuildEntities {
        return pudding.transaction {
            val guildData = DiscordGuilds.slice(
                DiscordGuilds.id,
                DiscordGuilds.roles,
                DiscordGuilds.channels,
                DiscordGuilds.emojis
            ).selectFirstOrNull {
                DiscordGuilds.id eq guildId.toLong()
            } ?: return@transaction EMPTY_GUILD_ENTITIES

            return@transaction GuildEntities(
                Json.decodeFromString<PuddingDiscordRolesMap>(guildData[DiscordGuilds.roles])
                    .values
                    .toList(),
                Json.decodeFromString<PuddingDiscordChannelsMap>(guildData[DiscordGuilds.channels])
                    .values
                    .toList(),
                Json.decodeFromString<PuddingDiscordEmojisMap>(guildData[DiscordGuilds.emojis])
                    .values
                    .toList(),
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
            val roles = DiscordGuilds.slice(DiscordGuilds.roles)
                .selectFirstOrNull {
                    DiscordGuilds.id eq guildId.toLong()
                }?.get(DiscordGuilds.roles) ?: return@transaction emptyList()

            return@transaction Json.decodeFromString<PuddingDiscordRolesMap>(roles)
                .values
                .filter { it.id in roleIds }
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
            val userRoleIds = DiscordGuildMembers
                .slice(DiscordGuildMembers.roles)
                .selectFirstOrNull { DiscordGuildMembers.guildId eq guildId.toLong() and (DiscordGuildMembers.userId eq userId.toLong()) }
                ?.get(DiscordGuildMembers.roles)
                ?.let {
                    Json.decodeFromString<PuddingDiscordRolesList>(it)
                } ?: emptyList()

            val guild = DiscordGuilds
                .slice(DiscordGuilds.roles, DiscordGuilds.channels)
                .selectFirstOrNull { DiscordGuilds.id eq guildId.toLong() }

            val rolesAsJson = guild?.get(DiscordGuilds.roles)
            val channelsAsJson = guild?.get(DiscordGuilds.channels)

            val guildRoles = rolesAsJson?.let { Json.decodeFromString<PuddingDiscordRolesMap>(it) } ?: emptyMap()
            val guildChannels = channelsAsJson?.let { Json.decodeFromString<PuddingDiscordChannelsMap>(it) } ?: emptyMap()

            val guildChannel = guildChannels[channelId.toString()]
            val everyoneRole = guildRoles[guildId.toString()]

            val userRoles = guildRoles
                .filter { it.key in userRoleIds }
                .values
                .toMutableList()

            if (everyoneRole != null) {
                userRoles.add(everyoneRole)
            } else {
                logger.warn { "Everyone role is null in $guildId! We will ignore it..." }
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
                if (everyoneRole != null) {
                    val everyonePermissionOverwrite = permissionOverwrites.firstOrNull { it.id == everyoneRole.id }
                    if (everyonePermissionOverwrite != null) {
                        permissions = permissions.minus(everyonePermissionOverwrite.deny)
                        permissions = permissions.plus(everyonePermissionOverwrite.allow)
                    }
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

    data class GuildEntities(
        val roles: List<DiscordRole>,
        val channels: List<DiscordChannel>,
        val emojis: List<DiscordEmoji>
    )
}