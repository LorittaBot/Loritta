package net.perfectdreams.loritta.cinnamon.discord.utils.entitycache

import dev.kord.common.DiscordBitSet
import dev.kord.common.entity.DiscordGuildMember
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.*
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji
import net.perfectdreams.loritta.morenitta.LorittaBot
import org.jetbrains.exposed.sql.*
import java.util.*

/**
 * Services related to Discord entity caching
 */
class DiscordCacheService(
    private val loritta: LorittaBot
) {
    private val lorittaDiscordConfig = loritta.config.loritta.discord

    fun getDiscordEntitiesOfGuild(guildId: Snowflake): GuildEntities {
        val guild = loritta.lorittaShards.getGuildById(guildId.value.toLong())

        return GuildEntities(
            guild?.roles ?: emptyList(),
            guild?.channels ?: emptyList(),
            guild?.emojis ?: emptyList()
        )
    }

    /**
     * Gets the [member]'s roles in [guildId]
     */
    suspend fun getRoles(guildId: Snowflake, member: DiscordGuildMember) = getRoles(guildId, member.roles)

    /**
     * Gets role informations of the following [roleIds] in [guildId]
     */
    suspend fun getRoles(guildId: Snowflake, roleIds: Collection<Snowflake>): List<Role> {
        return loritta.lorittaShards.getGuildById(guildId.value.toLong())?.roles?.filter { Snowflake(it.idLong) in roleIds } ?: return emptyList()
    }

    /**
     * Creates a [LazyCachedPermissions] class with the [guildId], [channelId] and with Loritta's user ID.
     *
     * It is lazy because the permission bitset is only retrieved after a [LazyCachedPermissions.hasPermission] check is triggered.
     *
     * It is cached because the permission bitset is cached, so after the permission bitset is already retrieved, it won't be retrieved again.
     */
    fun getLazyCachedLorittaPermissions(guildId: Snowflake, channelId: Snowflake) = LazyCachedPermissions(getPermissions(guildId, channelId, lorittaDiscordConfig.applicationId).permissions)

    /**
     * Creates a [LazyCachedPermissions] class with the [guildId], [channelId] and [userId].
     *
     * It is lazy because the permission bitset is only retrieved after a [LazyCachedPermissions.hasPermission] check is triggered.
     *
     * It is cached because the permission bitset is cached, so after the permission bitset is already retrieved, it won't be retrieved again.
     */
    fun getLazyCachedPermissions(guildId: Snowflake, channelId: Snowflake, userId: Snowflake) = LazyCachedPermissions(getPermissions(guildId, channelId, userId).permissions)

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
     * Gets [userId]'s permissions in [channelId] on [guildId].
     *
     * @see getLorittaPermissions
     */
    fun getPermissions(guildId: Snowflake, channelId: Snowflake, userId: Snowflake): PermissionsResult {
        val guild = loritta.lorittaShards.getGuildById(guildId.value.toLong()) ?: return PermissionsResult( // They aren't in the server, no need to continue then
            Permissions(),
            userNotInGuild = true, // This is actually unknown
            missingRoles = false, // This is actually "Unknown"
            missingChannels = false // This is also "Unknown"
        )

        val member = guild.getMemberById(userId.value.toLong()) ?: return PermissionsResult( // They aren't in the server, no need to continue then
            Permissions(),
            userNotInGuild = true, // This is actually unknown
            missingRoles = false, // This is actually "Unknown"
            missingChannels = false // This is also "Unknown"
        )

        val channel = guild.getGuildChannelById(channelId.value.toLong()) ?: return PermissionsResult( // They aren't in the server, no need to continue then
            Permissions(),
            userNotInGuild = false,
            missingRoles = false, // This is actually "Unknown"
            missingChannels = true
        )

        val permissions = member.getPermissions(channel)

        return PermissionsResult(
            Permissions(DiscordBitSet(net.dv8tion.jda.api.Permission.getRaw(permissions))),
            userNotInGuild = false,
            missingRoles = false,
            missingChannels = false
        )
    }

    /**
     * Gets the [userId]'s voice channel on [guildId], if they are connected to a voice channel
     *
     * @param guildId the guild's ID
     * @param userId  the user's ID
     * @return the voice channel ID, if they are connected to a voice channel
     */
    suspend fun getUserConnectedVoiceChannel(guildId: Snowflake, userId: Snowflake): VoiceChannel? {
        return loritta.lorittaShards.getGuildById(guildId.value.toLong())
            ?.getMemberById(userId.value.toLong())
            ?.voiceState
            ?.channel
            ?.asVoiceChannel()
    }

    fun getUserConnectedVoiceChannel(guildId: Long, userId: Long): VoiceChannel? {
        return loritta.lorittaShards.getGuildById(guildId)
            ?.getMemberById(userId)
            ?.voiceState
            ?.channel
            ?.asVoiceChannel()
    }

    data class PermissionsResult(
        val permissions: Permissions,
        val userNotInGuild: Boolean,
        val missingRoles: Boolean,
        val missingChannels: Boolean
    )

    data class GuildEntities(
        val roles: List<Role>,
        val channels: List<GuildChannel>,
        val emojis: List<RichCustomEmoji>
    )
}