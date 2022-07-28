package net.perfectdreams.loritta.cinnamon.discord.utils.entitycache

import dev.kord.common.entity.DiscordChannel
import dev.kord.common.entity.DiscordEmoji
import dev.kord.common.entity.DiscordGuildMember
import dev.kord.common.entity.DiscordRole
import dev.kord.common.entity.OverwriteType
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.rest.service.RestClient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.utils.PuddingDiscordRolesList
import net.perfectdreams.loritta.cinnamon.discord.utils.config.LorittaDiscordConfig
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.*
import net.perfectdreams.loritta.cinnamon.pudding.utils.HashEncoder
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.batchUpsert
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.upsert
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import java.util.*

/**
 * Services related to Discord entity caching
 */
class DiscordCacheService(
    private val rest: RestClient,
    private val lorittaDiscordConfig: LorittaDiscordConfig,
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

        return pudding.transaction {
            var missingRoles = false
            var missingChannels = false

            val guildRolesResultRow = DiscordGuildMembers
                .slice(DiscordGuildMembers.roles)
                .selectFirstOrNull { DiscordGuildMembers.guildId eq guildId.toLong() and (DiscordGuildMembers.userId eq userId.toLong()) }
                ?: return@transaction PermissionsResult( // They aren't in the server, no need to continue then
                    permissions,
                    userNotInGuild = true,
                    missingRoles = false, // This is actually "Unknown"
                    missingChannels = false // This is also "Unknown"
                )

            val userRoleIds = Json.decodeFromString<PuddingDiscordRolesList>(guildRolesResultRow.get(DiscordGuildMembers.roles)) + guildId.toString() // Because the user always have the "@everyone" role!

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

            return@transaction PermissionsResult(
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
        return pudding.transaction {
            DiscordVoiceStates.slice(DiscordVoiceStates.channel).selectFirstOrNull {
                DiscordVoiceStates.guild eq guildId.toLong() and (DiscordVoiceStates.user eq userId.toLong())
            }?.get(DiscordVoiceStates.channel)?.let { Snowflake(it) }
        }
    }

    /**
     * **THIS SHOULD BE CALLED WITHIN A TRANSACTION!!**
     */
    fun createOrUpdateGuild(
        guildId: Snowflake,
        guildName: String,
        guildIcon: String?,
        guildOwnerId: Snowflake,
        guildRoles: List<DiscordRole>,
        guildChannels: List<DiscordChannel>?,
        guildEmojis: List<DiscordEmoji>
    ) {
        val guildIdAsLong = guildId.toLong()

        // Does not exist, let's insert it
        DiscordGuilds.upsert(DiscordGuilds.id) {
            it[DiscordGuilds.id] = guildIdAsLong
            it[DiscordGuilds.name] = guildName
            it[DiscordGuilds.icon] = guildIcon
            it[DiscordGuilds.ownerId] = guildOwnerId.toLong()
        }

        updateEntitiesInDatabaseIfNeeded(
            DiscordRoles,
            DiscordRoles.guild,
            DiscordRoles.dataHashCode,
            DiscordRoles.role,
            guildId,
            guildRoles,
            DiscordRole::id,
            DiscordRoles.guild,
            DiscordRoles.role
        ) { it, role, hash ->
            it[DiscordRoles.guild] = guildIdAsLong
            it[DiscordRoles.role] = role.id.toLong()
            it[DiscordRoles.dataHashCode] = hash
            it[DiscordRoles.data] = Json.encodeToString(role)
        }

        if (guildChannels != null) {
            updateEntitiesInDatabaseIfNeeded(
                DiscordChannels,
                DiscordChannels.guild,
                DiscordChannels.dataHashCode,
                DiscordChannels.channel,
                guildId,
                guildChannels,
                DiscordChannel::id,
                DiscordChannels.guild,
                DiscordChannels.channel
            ) { it, channel, hash ->
                it[DiscordChannels.guild] = guildIdAsLong
                it[DiscordChannels.channel] = channel.id.toLong()
                it[DiscordChannels.dataHashCode] = hash
                it[DiscordChannels.data] = Json.encodeToString(channel)
            }
        }

        updateGuildEmojis(guildId, guildEmojis)
    }

    /**
     * **THIS SHOULD BE CALLED WITHIN A TRANSACTION!!**
     */
    fun updateGuildEmojis(guildId: Snowflake, guildEmojis: List<DiscordEmoji>) {
        val guildIdAsLong = guildId.toLong()

        updateEntitiesInDatabaseIfNeeded(
            DiscordEmojis,
            DiscordEmojis.guild,
            DiscordEmojis.dataHashCode,
            DiscordEmojis.emoji,
            guildId,
            guildEmojis,
            { it.id!! },
            DiscordEmojis.guild,
            DiscordEmojis.emoji
        ) { it, emoji, hash ->
            it[DiscordEmojis.guild] = guildIdAsLong
            it[DiscordEmojis.emoji] = emoji.id!!.toLong() // Shouldn't be null because guild emojis always have IDs
            it[DiscordEmojis.dataHashCode] = hash
            it[DiscordEmojis.data] = Json.encodeToString(emoji)
        }
    }

    /**
     * **THIS SHOULD BE CALLED WITHIN A TRANSACTION!!**
     *
     * Check if the stored [entities] contains the same elements and, if not, batch upsert and delete outdated entries
     *
     * This stores a [dataHashColumn] of the entity, to optimize the upserting procedure to avoid multiple upserts.
     *
     * This reduces the query time quite a bit if we don't need to update the roles/channels/emojis
     */
    inline fun <T : Table, reified E> updateEntitiesInDatabaseIfNeeded(
        table: T,
        guildColumn: Column<Long>,
        dataHashColumn: Column<Int>,
        entityIdColumn: Column<Long>,
        guildId: Snowflake,
        entities: List<E>,
        crossinline entityId: (E) -> (Snowflake),
        vararg keys: Column<*>,
        noinline body: T.(BatchInsertStatement, E, Int) -> Unit
    ) {
        val guildIdAsLong = guildId.toLong()

        val storedEntitiesHashCodes = table.slice(dataHashColumn)
            .select {
                guildColumn eq guildIdAsLong
            }
            .map { it[dataHashColumn] }

        val hashedEntities = entities.associate { entityId.invoke(it) to hashEntity(it) }

        val currentEntitiesHashes = hashedEntities.values

        val requiresUpdates = !currentEntitiesHashes.containsSameElements(storedEntitiesHashCodes)

        if (requiresUpdates) {
            val requiresUpdateEntities = entities.filter { hashedEntities[entityId.invoke(it)]!! !in storedEntitiesHashCodes }
            val allEntityIds = entities.map { entityId.invoke(it) }

            if (requiresUpdateEntities.isNotEmpty())
                table.batchUpsert(requiresUpdateEntities, *keys, body = { it, e ->
                    body.invoke(table, it, e, hashedEntities[entityId.invoke(e)]!!)
                })

            // Delete unknown roles
            table.deleteWhere { guildColumn eq guildIdAsLong and (entityIdColumn notInList allEntityIds.map { it.toLong() }) }
        }
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