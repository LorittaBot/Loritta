package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules

import com.github.benmanes.caffeine.cache.Caffeine
import dev.kord.common.entity.*
import dev.kord.common.entity.optional.value
import dev.kord.gateway.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.DiscordGatewayEventsProcessor
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.GatewayProxyEventContext
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.HashEncoder
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.batchUpsert
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.upsert
import net.perfectdreams.loritta.cinnamon.platform.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager
import pw.forst.exposed.insertOrUpdate
import java.util.*
import java.util.concurrent.TimeUnit

class DiscordCacheModule(private val m: DiscordGatewayEventsProcessor) : ProcessDiscordEventsModule() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Used to avoid updating the same information at the same time, causing "Could not serialize access due to concurrent update"
     */
    private val mutexes = Caffeine.newBuilder()
        .expireAfterAccess(1L, TimeUnit.MINUTES)
        .build<String, Mutex>()
        .asMap()

    /**
     * Used to avoid sending a transaction on a MessageCreate event just to update the user's roles
     */
    private val userRolesHashes = Caffeine.newBuilder()
        .expireAfterAccess(1L, TimeUnit.MINUTES)
        .build<Snowflake, Int>()
        .asMap()

    private suspend inline fun withMutex(vararg ids: Snowflake, action: () -> Unit) = mutexes.getOrPut(ids.joinToString(":")) { Mutex() }.withLock(action = action)

    override suspend fun processEvent(context: GatewayProxyEventContext): ModuleResult {
        when (val event = context.event) {
            is GuildCreate -> {
                // logger.info { "Howdy ${event.guild.id} (${event.guild.name})! Is unavailable? ${event.guild.unavailable}" }

                if (!event.guild.unavailable.discordBoolean) {
                    val start = System.currentTimeMillis()

                    val guildId = event.guild.id
                    val guildName = event.guild.name
                    val guildIcon = event.guild.icon
                    val guildOwnerId = event.guild.ownerId
                    val guildRoles = event.guild.roles
                    val guildChannels = event.guild.channels.value!! // Shouldn't be null in a GUILD_CREATE event
                    val guildEmojis = event.guild.emojis
                    // If your bot does not have the GUILD_PRESENCES Gateway Intent, or if the guild has over 75k members, members and presences returned in this event will only contain your bot and users in voice channels.
                    val guildMembers = event.guild.members.value ?: emptyList()
                    val guildVoiceStates = event.guild.voiceStates.value ?: emptyList()

                    val lorittaVoiceState = guildVoiceStates.firstOrNull { it.userId == Snowflake(m.config.discord.applicationId) }
                    if (lorittaVoiceState != null) {
                        // Wait... that's us! omg omg omg
                        val lorittaVoiceConnection = m.voiceConnections[guildId]

                        if (lorittaVoiceConnection == null) {
                            // B-but... we shouldn't be here!? Uhhh, meow? Time to disconnect maybe??
                            logger.warn { "Looks like we are connected @ $guildId but we don't have any active voice connections here! We will disconnect from the voice channel to avoid issues..." }
                            val gatewayProxy = m.getProxiedKordGatewayForGuild(guildId)
                            gatewayProxy?.send(
                                UpdateVoiceStatus(
                                    guildId,
                                    null,
                                    selfMute = false,
                                    selfDeaf = false
                                )
                            )
                        }
                    }

                    withMutex(guildId) {
                        m.guildCreateServices.transaction {
                            disableSynchronousCommit()

                            createOrUpdateGuild(
                                guildId,
                                guildName,
                                guildIcon,
                                guildOwnerId,
                                guildRoles,
                                guildChannels,
                                guildEmojis
                            )

                            for (member in guildMembers) {
                                createOrUpdateGuildMember(
                                    guildId,
                                    member.user.value!!.id,
                                    member
                                )
                            }

                            // Delete all voice states
                            DiscordVoiceStates.deleteWhere {
                                DiscordVoiceStates.guild eq guildId.toLong()
                            }

                            // Reinsert them
                            if (guildVoiceStates.isNotEmpty()) {
                                DiscordVoiceStates.batchUpsert(
                                    guildVoiceStates,
                                    DiscordVoiceStates.user,
                                    DiscordVoiceStates.guild
                                ) { it, voiceState ->
                                    it[DiscordVoiceStates.guild] =
                                        guildId.toLong() // The voiceState.guildId is missing on a GuildCreate event!
                                    it[DiscordVoiceStates.channel] =
                                        voiceState.channelId!!.toLong() // Also shouldn't be null because they are in a channel
                                    it[DiscordVoiceStates.user] = voiceState.userId.toLong()
                                    it[DiscordVoiceStates.dataHashCode] = hashEntity(voiceState)
                                    it[DiscordVoiceStates.data] = Json.encodeToString(voiceState)
                                }
                            }
                        }
                    }

                    logger.info { "GuildCreate for $guildId took ${System.currentTimeMillis() - start}ms" }
                }
            }
            is GuildUpdate -> {
                if (!event.guild.unavailable.discordBoolean) {
                    val start = System.currentTimeMillis()

                    val guildId = event.guild.id
                    val guildName = event.guild.name
                    val guildIcon = event.guild.icon
                    val guildOwnerId = event.guild.ownerId
                    val guildRoles = event.guild.roles
                    val guildChannels = event.guild.channels.value
                    val guildEmojis = event.guild.emojis

                    withMutex(guildId) {
                        m.services.transaction {
                            disableSynchronousCommit()

                            createOrUpdateGuild(
                                guildId,
                                guildName,
                                guildIcon,
                                guildOwnerId,
                                guildRoles,
                                guildChannels,
                                guildEmojis
                            )
                        }
                    }

                    logger.info { "GuildUpdate for $guildId took ${System.currentTimeMillis() - start}ms" }
                }
            }
            is GuildEmojisUpdate -> {
                val guildId = event.emoji.guildId
                val discordEmojis = event.emoji.emojis

                withMutex(guildId) {
                    m.services.transaction {
                        disableSynchronousCommit()

                        updateGuildEmojis(guildId, discordEmojis)
                    }
                }
            }
            is MessageCreate -> {
                val guildId = event.message.guildId.value
                val member = event.message.member.value

                if (guildId != null && member != null) {
                    // To avoid unnecessary database updates just because someone sent a message in chat, we will store a hash of their roles in memory
                    // If the role list hash doesn't match, *then* we send a transaction updating the user's information
                    val userRoleHash = userRolesHashes[event.message.author.id]

                    if (userRoleHash != member.roles.hashCode()) {
                        withMutex(guildId, event.message.author.id) {
                            userRolesHashes[event.message.author.id] = member.roles.hashCode()

                            m.services.transaction {
                                disableSynchronousCommit()

                                createOrUpdateGuildMember(guildId, event.message.author.id, member)
                            }
                        }
                    }
                }
            }
            is GuildMemberAdd -> {
                withMutex(event.member.guildId, event.member.user.value!!.id) {
                    m.services.transaction {
                        disableSynchronousCommit()

                        createOrUpdateGuildMember(event.member)
                    }
                }
            }
            is GuildMemberUpdate -> {
                withMutex(event.member.guildId, event.member.user.id) {
                    m.services.transaction {
                        disableSynchronousCommit()

                        createOrUpdateGuildMember(event.member)
                    }
                }
            }
            is GuildMemberRemove -> {
                withMutex(event.member.guildId, event.member.user.id) {
                    m.services.transaction {
                        disableSynchronousCommit()

                        deleteGuildMember(event.member)
                    }
                }
            }
            is ChannelCreate -> {
                val guildId = event.channel.guildId.value
                if (guildId != null)
                    withMutex(guildId, event.channel.id) {
                        m.services.transaction {
                            disableSynchronousCommit()

                            createOrUpdateGuildChannel(guildId, event.channel)
                        }
                    }
            }
            is ChannelUpdate -> {
                val guildId = event.channel.guildId.value
                if (guildId != null)
                    withMutex(guildId, event.channel.id) {
                        m.services.transaction {
                            disableSynchronousCommit()

                            createOrUpdateGuildChannel(guildId, event.channel)
                        }
                    }
            }
            is ChannelDelete -> {
                val guildId = event.channel.guildId.value
                if (guildId != null)
                    withMutex(guildId, event.channel.id) {
                        m.services.transaction {
                            disableSynchronousCommit()

                            deleteGuildChannel(guildId, event.channel)
                        }
                    }
            }
            is GuildRoleCreate -> {
                withMutex(event.role.guildId, event.role.role.id) {
                    m.services.transaction {
                        disableSynchronousCommit()

                        createOrUpdateRole(event.role.guildId, event.role.role)
                    }
                }
            }
            is GuildRoleUpdate -> {
                withMutex(event.role.guildId, event.role.role.id) {
                    m.services.transaction {
                        disableSynchronousCommit()

                        createOrUpdateRole(event.role.guildId, event.role.role)
                    }
                }
            }
            is GuildRoleDelete -> {
                withMutex(event.role.guildId, event.role.id) {
                    m.services.transaction {
                        disableSynchronousCommit()

                        deleteRole(event.role.guildId, event.role.id)
                    }
                }
            }
            is GuildDelete -> {
                // If the unavailable field is not set, the user/bot was removed from the guild.
                if (event.guild.unavailable.value == null) {
                    logger.info { "Someone removed me @ ${event.guild.id}! :(" }
                    withMutex(event.guild.id) {
                        m.services.transaction {
                            disableSynchronousCommit()

                            removeGuildData(event.guild.id)
                        }
                    }
                }
            }
            is VoiceStateUpdate -> {
                val guildId = event.voiceState.guildId.value!! // Shouldn't be null here
                val channelId = event.voiceState.channelId
                val userId = event.voiceState.userId

                if (userId == Snowflake(m.config.discord.applicationId)) {
                    // Wait... that's us! omg omg omg
                    val lorittaVoiceConnection = m.voiceConnections[guildId]
                    if (lorittaVoiceConnection != null) {
                        if (channelId != null) {
                            logger.info { "We were moved @ $guildId to $channelId, updating our voice connection to match it..." }
                            lorittaVoiceConnection.channelId = channelId
                        } else {
                            logger.info { "We were removed from a voice channel @ $guildId, shutting down our voice connection..." }
                            m.shutdownVoiceConnection(guildId, lorittaVoiceConnection)
                        }
                    }
                }

                withMutex(guildId) {
                    val voiceState = event.voiceState
                    m.services.transaction {
                        disableSynchronousCommit()

                        // Delete all voice states related to the user
                        DiscordVoiceStates.deleteWhere {
                            DiscordVoiceStates.guild eq guildId.toLong() and (DiscordVoiceStates.user eq voiceState.userId.toLong())
                        }

                        if (channelId != null) {
                            // Reinsert them
                            DiscordVoiceStates.upsert(
                                DiscordVoiceStates.user,
                                DiscordVoiceStates.guild
                            ) {
                                it[DiscordVoiceStates.guild] = guildId.toLong() // The voiceState.guildId is missing on a GuildCreate event!
                                it[DiscordVoiceStates.channel] = channelId.toLong() // Also shouldn't be null because they are in a channel
                                it[DiscordVoiceStates.user] = userId.toLong()
                                it[DiscordVoiceStates.dataHashCode] = hashEntity(voiceState)
                                it[DiscordVoiceStates.data] = Json.encodeToString(voiceState)
                            }
                        }
                    }
                }
            }
            else -> {}
        }
        return ModuleResult.Continue
    }

    private fun createOrUpdateGuild(
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

    private fun createOrUpdateGuildMember(guildMember: DiscordAddedGuildMember) {
        createOrUpdateGuildMember(
            guildMember.guildId,
            guildMember.user.value!!.id,
            guildMember.roles
        )
    }

    private fun createOrUpdateGuildMember(guildMember: DiscordUpdatedGuildMember) {
        createOrUpdateGuildMember(
            guildMember.guildId,
            guildMember.user.id,
            guildMember.roles
        )
    }

    private fun createOrUpdateGuildMember(guildId: Snowflake, userId: Snowflake, guildMember: DiscordGuildMember) {
        createOrUpdateGuildMember(
            guildId,
            userId,
            guildMember.roles
        )
    }

    private fun createOrUpdateGuildMember(
        guildId: Snowflake,
        userId: Snowflake,
        roles: List<Snowflake>
    ) {
        DiscordGuildMembers.insertOrUpdate(DiscordGuildMembers.guildId, DiscordGuildMembers.userId) {
            it[DiscordGuildMembers.guildId] = guildId.toLong()
            it[DiscordGuildMembers.userId] = userId.toLong()
            it[DiscordGuildMembers.roles] = Json.encodeToString(roles.map { it.toString() })
        }
    }

    private fun deleteGuildMember(guildMember: DiscordRemovedGuildMember) {
        DiscordGuildMembers.deleteWhere {
            DiscordGuildMembers.guildId eq guildMember.guildId.toLong() and (DiscordGuildMembers.userId eq guildMember.user.id.toLong())
        }
    }

    private fun createOrUpdateGuildChannel(guildId: Snowflake, channel: DiscordChannel) {
        DiscordChannels.upsert(DiscordChannels.guild, DiscordChannels.channel) {
            it[DiscordChannels.guild] = guildId.toLong()
            it[DiscordChannels.channel] = channel.id.toLong()
            it[DiscordChannels.dataHashCode] = hashEntity(channel)
            it[DiscordChannels.data] = Json.encodeToString(channel)
        }
    }

    private fun updateGuildEmojis(guildId: Snowflake, guildEmojis: List<DiscordEmoji>) {
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

    private fun createOrUpdateRole(guildId: Snowflake, role: DiscordRole) {
        DiscordRoles.upsert(DiscordRoles.guild, DiscordRoles.role) {
            it[DiscordRoles.guild] = guildId.toLong()
            it[DiscordRoles.role] = role.id.toLong()
            it[DiscordRoles.dataHashCode] = hashEntity(role)
            it[DiscordRoles.data] = Json.encodeToString(role)
        }
    }

    private fun deleteRole(guildId: Snowflake, roleId: Snowflake) {
        DiscordRoles.deleteWhere {
            DiscordRoles.guild eq guildId.toLong() and (DiscordRoles.role eq roleId.toLong())
        }
    }

    private fun deleteGuildChannel(guildId: Snowflake, channel: DiscordChannel) {
        DiscordChannels.deleteWhere {
            DiscordChannels.guild eq guildId.toLong() and (DiscordChannels.channel eq channel.id.toLong())
        }
    }

    private fun removeGuildData(guildId: Snowflake) {
        logger.info { "Removing $guildId's cached data..." }
        val guildIdAsLong = guildId.toLong()

        DiscordGuildMembers.deleteWhere {
            DiscordGuildMembers.guildId eq guildIdAsLong
        }

        DiscordChannels.deleteWhere {
            DiscordChannels.guild eq guildIdAsLong
        }

        DiscordEmojis.deleteWhere {
            DiscordEmojis.guild eq guildIdAsLong
        }

        DiscordRoles.deleteWhere {
            DiscordRoles.guild eq guildIdAsLong
        }

        DiscordGuilds.deleteWhere {
            DiscordGuilds.id eq guildIdAsLong
        }
    }

    /**
     * Check if the stored [entities] contains the same elements and, if not, batch upsert and delete outdated entries
     *
     * This stores a [dataHashColumn] of the entity, to optimize the upserting procedure to avoid multiple upserts.
     *
     * This reduces the query time quite a bit if we don't need to update the roles/channels/emojis
     */
    private inline fun <T : Table, reified E> updateEntitiesInDatabaseIfNeeded(
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
    private inline fun <reified T> hashEntity(value: T): Int {
        // We use our own custom hash encoder because ProtoBuf can't encode the "Optional" fields, because it can't serialize null values
        // on a field that isn't marked as null
        val encoder = HashEncoder()
        encoder.encodeSerializableValue(serializer(), value)
        return Objects.hash(*encoder.list.toTypedArray())
    }

    // https://stackoverflow.com/a/58310635/7271796
    private fun <T> Collection<T>.containsSameElements(other: Collection<T>): Boolean {
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

    /**
     * Disables synchronous commit on the current transaction.
     *
     * This gives a performance boost, however because the transaction is async, you may lose data if PostgreSQL crashes!
     */
    private fun disableSynchronousCommit() = TransactionManager.current().exec("SET LOCAL synchronous_commit = 'off';")
}