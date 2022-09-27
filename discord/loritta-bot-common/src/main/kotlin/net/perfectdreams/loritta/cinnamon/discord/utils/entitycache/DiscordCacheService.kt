package net.perfectdreams.loritta.cinnamon.discord.utils.entitycache

import com.github.luben.zstd.Zstd
import com.github.luben.zstd.ZstdDictCompress
import com.github.luben.zstd.ZstdDictDecompress
import dev.kord.common.entity.DiscordChannel
import dev.kord.common.entity.DiscordEmoji
import dev.kord.common.entity.DiscordGuildMember
import dev.kord.common.entity.DiscordRole
import dev.kord.common.entity.OverwriteType
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import mu.KotlinLogging
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.utils.redis.hgetAllByteArray
import net.perfectdreams.loritta.cinnamon.discord.utils.redis.hgetByteArray
import net.perfectdreams.loritta.cinnamon.discord.utils.redis.hsetByteArrayOrDelIfMapIsEmpty
import net.perfectdreams.loritta.cinnamon.pudding.utils.HashEncoder
import org.jetbrains.exposed.sql.*
import java.nio.ByteBuffer
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
    val zstdDictionaries = ZstdDictionaries()

    suspend fun getDiscordEntitiesOfGuild(guildId: Snowflake): GuildEntities {
        return loritta.redisConnection {
            val roles = it.hgetAllByteArray(loritta.redisKeys.discordGuildRoles(guildId))
                .values
                .map {
                    decodeFromBinary<DiscordRole>(it)
                }

            val channels = it.hgetAllByteArray(loritta.redisKeys.discordGuildChannels(guildId))
                .values
                .map {
                    decodeFromBinary<DiscordChannel>(it)
                }

            val emojis = it.hgetAllByteArray(loritta.redisKeys.discordGuildEmojis(guildId))
                .values
                .map {
                    decodeFromBinary<DiscordEmoji>(it)
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
    suspend fun getRoles(guildId: Snowflake, roleIds: Collection<Snowflake>): List<DiscordRole> {
        return loritta.redisConnection {
            it.hgetAllByteArray(loritta.redisKeys.discordGuildRoles(guildId))
                .filterKeys { Snowflake(it.toLong()) in roleIds }
                .map {
                    decodeFromBinary(it.value)
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
    fun getLazyCachedLorittaPermissions(guildId: Snowflake, channelId: Snowflake) = LazyCachedPermissions(rest, loritta, this, guildId, channelId, lorittaDiscordConfig.applicationId)

    /**
     * Creates a [LazyCachedPermissions] class with the [guildId], [channelId] and [userId].
     *
     * It is lazy because the permission bitset is only retrieved after a [LazyCachedPermissions.hasPermission] check is triggered.
     *
     * It is cached because the permission bitset is cached, so after the permission bitset is already retrieved, it won't be retrieved again.
     */
    fun getLazyCachedPermissions(guildId: Snowflake, channelId: Snowflake, userId: Snowflake) = LazyCachedPermissions(rest, loritta, this, guildId, channelId, userId)

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
    suspend fun getPermissions(guildId: Snowflake, channelId: Snowflake, userId: Snowflake): PermissionsResult {
        // Create an empty permissions object
        var permissions = Permissions()

        return loritta.redisConnection {
            val discordGuildMember = it
                .hgetByteArray(loritta.redisKeys.discordGuildMembers(guildId), userId.toString())
                ?: return@redisConnection PermissionsResult( // They aren't in the server, no need to continue then
                    permissions,
                    userNotInGuild = true,
                    missingRoles = false, // This is actually "Unknown"
                    missingChannels = false // This is also "Unknown"
                )

            val userRoleIds = decodeFromBinary<PuddingGuildMember>(discordGuildMember).roles

            val userRoles = userRoleIds.mapNotNull { snowflake ->
                it.hgetByteArray(loritta.redisKeys.discordGuildRoles(guildId), snowflake.toString())
            }.map { decodeFromBinary<DiscordRole>(it) }

            var missingRoles = false
            var missingChannels = false

            val guildChannel = it.hgetByteArray(loritta.redisKeys.discordGuildChannels(guildId), channelId.toString())
                ?.let { decodeFromBinary<DiscordChannel>(it) }

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

            return@redisConnection PermissionsResult(
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
        return loritta.redisConnection {
            it.hget(loritta.redisKeys.discordGuildVoiceStates(guildId), userId.toString())
                ?.let { Json.decodeFromString<PuddingGuildVoiceState>(it) }
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
                it.id.toString() to encodeToBinary(it, ZstdDictionaries.Dictionary.ROLES_V1)
            }
        )

        if (guildChannels != null) {
            redisTransaction.hsetByteArrayOrDelIfMapIsEmpty(
                loritta.redisKeys.discordGuildChannels(guildId),
                guildChannels.associate {
                    it.id.toString() to encodeToBinary(it, ZstdDictionaries.Dictionary.CHANNELS_V1)
                }
            )
        }

        redisTransaction.hsetByteArrayOrDelIfMapIsEmpty(
            loritta.redisKeys.discordGuildEmojis(guildId),
            guildEmojis.associate {
                // Guild Emojis always have an ID
                it.id!!.toString() to encodeToBinary(it, ZstdDictionaries.Dictionary.EMOJIS_V1)
            }
        )
    }

    suspend fun updateGuildEmojis(guildId: Snowflake, guildEmojis: List<DiscordEmoji>) {
        loritta.redisConnection {
            it.hsetByteArrayOrDelIfMapIsEmpty(
                loritta.redisKeys.discordGuildEmojis(guildId),
                guildEmojis.associate {
                    // Guild Emojis always have an ID
                    it.id!!.toString() to encodeToBinary(it, ZstdDictionaries.Dictionary.EMOJIS_V1)
                }
            )
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

    fun compressWithZstd(payload: String) = Zstd.compress(payload.toByteArray(Charsets.UTF_8), 2)
    fun compressWithZstd(payload: String, dictCompress: ZstdDictCompress) = Zstd.compress(payload.toByteArray(Charsets.UTF_8), dictCompress)
    fun decompressWithZstd(payload: ByteArray): ByteArray = Zstd.decompress(payload, Zstd.decompressedSize(payload).toInt())
    fun decompressWithZstd(payload: ByteArray, dictDecompress: ZstdDictDecompress): ByteArray = Zstd.decompress(payload, dictDecompress, Zstd.decompressedSize(payload).toInt())

    /**
     * Encodes and compresses the [payload] to binary, useful to be stored in an in-memory database (such as Redis)
     */
    inline fun <reified T> encodeToBinary(
        payload: T,
        dictionary: ZstdDictionaries.Dictionary
    ): ByteArray {
        val zstdCompress = when (dictionary) {
            ZstdDictionaries.Dictionary.NO_DICTIONARY -> null
            ZstdDictionaries.Dictionary.ROLES_V1 -> zstdDictionaries.rolesV1.compress
            ZstdDictionaries.Dictionary.CHANNELS_V1 -> zstdDictionaries.channelsV1.compress
            ZstdDictionaries.Dictionary.EMOJIS_V1 -> zstdDictionaries.emojisV1.compress
        }

        val compressedWithZstd = if (zstdCompress == null)
            compressWithZstd(Json.encodeToString<T>(payload))
        else
            compressWithZstd(Json.encodeToString<T>(payload), zstdCompress)

        val header = LorittaCompressionHeader(0, dictionary)
        val headerAsByteArray = ProtoBuf.encodeToByteArray(header)

        val newArray = ByteArray(4 + headerAsByteArray.size + 4 + compressedWithZstd.size)
        val byteBuf = ByteBuffer.wrap(newArray)
        byteBuf.putInt(headerAsByteArray.size)
        byteBuf.put(headerAsByteArray)
        byteBuf.putInt(compressedWithZstd.size)
        byteBuf.put(compressedWithZstd)

        return newArray
    }

    /**
     * Decodes and decompresses the [payload] from binary, encoded with [encodeToBinary]
     */
    inline fun <reified T> decodeFromBinary(payload: ByteArray): T {
        val byteBuf = ByteBuffer.wrap(payload)

        // Loritta's Compression Header
        val headerLengthInBytes = byteBuf.int
        val headerBytes = ByteArray(headerLengthInBytes)
        byteBuf.get(headerBytes)

        val compressionHeader = ProtoBuf.decodeFromByteArray<LorittaCompressionHeader>(headerBytes)

        if (compressionHeader.version != 0)
            error("Unknown compression version ${compressionHeader.version}!")

        val zstdDecompress = when (compressionHeader.dictionaryId) {
            ZstdDictionaries.Dictionary.NO_DICTIONARY -> null
            ZstdDictionaries.Dictionary.ROLES_V1 -> zstdDictionaries.rolesV1.decompress
            ZstdDictionaries.Dictionary.CHANNELS_V1 -> zstdDictionaries.channelsV1.decompress
            ZstdDictionaries.Dictionary.EMOJIS_V1 -> zstdDictionaries.emojisV1.decompress
        }

        val zstdPayloadLength = byteBuf.int
        val zstdPayload = ByteArray(zstdPayloadLength)
        byteBuf.get(zstdPayload)

        val decompressed = if (zstdDecompress == null)
            decompressWithZstd(zstdPayload)
        else
            decompressWithZstd(zstdPayload, zstdDecompress)

        val byteArrayAsString = decompressed.toString(Charsets.UTF_8)
        return Json.decodeFromString<T>(byteArrayAsString)
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

    @Serializable
    data class LorittaCompressionHeader(
        val version: Int,
        val dictionaryId: ZstdDictionaries.Dictionary
    )
}