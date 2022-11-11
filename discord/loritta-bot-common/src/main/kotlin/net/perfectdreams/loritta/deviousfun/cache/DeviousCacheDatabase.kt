package net.perfectdreams.loritta.deviousfun.cache

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.exposedpowerutils.sql.batchUpsert
import net.perfectdreams.exposedpowerutils.sql.transaction
import net.perfectdreams.exposedpowerutils.sql.upsert
import net.perfectdreams.loritta.cinnamon.discord.utils.scheduleCoroutineAtFixedRate
import net.perfectdreams.loritta.deviouscache.data.*
import net.perfectdreams.loritta.deviousfun.tables.*
import net.perfectdreams.loritta.deviousfun.utils.DeviousGuildDataWrapper
import net.perfectdreams.loritta.deviousfun.utils.GuildAndUserPair
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class DeviousCacheDatabase(
    val cacheManager: DeviousCacheManager,
    val database: Database
) {
    companion object {
        private val logger = KotlinLogging.logger {}

        val cacheTables = listOf(
            Users,
            Guilds,
            GuildChannels,
            GuildMembers,
            GuildRoles,
            GuildEmojis,
            GuildVoiceStates,
            GatewaySessions
        )

        /**
         * Creates a database for entity persistence
         *
         * The parameters are used to improve throughput
         * * journal_mode = WAL improves performance
         * * synchronous = We don't care about data loss
         * * temp_store = We don't want to store indexes in memory, we already have the data stored in memory
         * * locking_mode = reduces the number of syscalls required, only one process (the Loritta instance) will access the database
         */
        fun createCacheDatabase(shardId: Int) = Database.connect("jdbc:sqlite:cache/lori_devious_shard_$shardId.db?journal_mode=wal&synchronous=off&mmap_size=30000000000&temp_store=0&locking_mode=exclusive")
    }

    private val dirtyEntities = DirtyEntitiesWrapper()
    private val mutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.Default)
    private val queuedActions = LinkedBlockingQueue<Job>()

    /**
     * Checks if this DeviousCacheManager instance is active
     *
     * If this is false, all cache requests should fail
     */
    var isActive = true

    /**
     * Queues an action to be added to the [DirtyEntitiesWrapper], access synchronized via [mutex].
     */
    fun queue(action: DirtyEntitiesWrapper.() -> (Unit)) {
        if (!isActive)
            return

        val job = scope.launch {
            mutex.withLock {
                action.invoke(dirtyEntities)
            }
        }
        queuedActions.add(job)
        job.invokeOnCompletion {
            queuedActions.remove(job)
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun runPendingQueries() {
        val dirtyUsers: Map<LightweightSnowflake, DeviousUserData>
        val removedRoles: Set<LightweightSnowflake>
        val dirtyRoles: Map<LightweightSnowflake, Map<LightweightSnowflake, DeviousRoleData>>
        val removedMembers: Set<GuildAndUserPair>
        val dirtyMembers: Map<GuildAndUserPair, DeviousMemberData>
        val removedEmojis: Set<LightweightSnowflake>
        val dirtyEmojis: Map<LightweightSnowflake, Map<LightweightSnowflake, DeviousGuildEmojiData>>
        val removedChannels: Set<LightweightSnowflake>
        val dirtyChannels: Map<LightweightSnowflake, Map<LightweightSnowflake, DeviousChannelData>>
        val removedGuilds: Set<LightweightSnowflake>
        val dirtyGuilds: Map<LightweightSnowflake, DeviousGuildDataWrapper>
        val removedVoiceStates: Set<LightweightSnowflake>
        val dirtyVoiceStates: Map<LightweightSnowflake, Map<LightweightSnowflake, DeviousVoiceStateData>>

        mutex.withLock {
            dirtyUsers = dirtyEntities.filterNotNull(dirtyEntities.users)

            removedMembers = dirtyEntities.filterNull(dirtyEntities.members)
            dirtyMembers = dirtyEntities.filterNotNull(dirtyEntities.members)

            removedRoles = dirtyEntities.filterNull(dirtyEntities.roles)
            dirtyRoles = dirtyEntities.filterNotNull(dirtyEntities.roles)

            removedEmojis = dirtyEntities.filterNull(dirtyEntities.emojis)
            dirtyEmojis = dirtyEntities.filterNotNull(dirtyEntities.emojis)

            removedChannels = dirtyEntities.filterNull(dirtyEntities.guildChannels)
            dirtyChannels = dirtyEntities.filterNotNull(dirtyEntities.guildChannels)

            removedGuilds = dirtyEntities.filterNull(dirtyEntities.guilds)
            dirtyGuilds = dirtyEntities.filterNotNull(dirtyEntities.guilds)

            removedVoiceStates = dirtyEntities.filterNull(dirtyEntities.voiceStates)
            dirtyVoiceStates = dirtyEntities.filterNotNull(dirtyEntities.voiceStates)

            dirtyEntities.users.clear()
            dirtyEntities.members.clear()
            dirtyEntities.roles.clear()
            dirtyEntities.emojis.clear()
            dirtyEntities.guildChannels.clear()
            dirtyEntities.guilds.clear()
            dirtyEntities.voiceStates.clear()
        }

        val time = measureTime {
            transaction(Dispatchers.IO, database) {
                if (dirtyUsers.isNotEmpty())
                    logger.info { "Trying to persist ${dirtyUsers.size} dirty users of shard ${cacheManager.deviousGateway.shardId}" }
                if (removedMembers.isNotEmpty())
                    logger.info { "${removedMembers.size} removed members of shard ${cacheManager.deviousGateway.shardId}" }
                if (dirtyMembers.isNotEmpty())
                    logger.info { "${dirtyMembers.size} dirty members of shard ${cacheManager.deviousGateway.shardId}" }
                if (removedRoles.isNotEmpty())
                    logger.info { "${removedRoles.size} removed roles of shard ${cacheManager.deviousGateway.shardId}" }
                if (dirtyRoles.isNotEmpty())
                    logger.info { "${dirtyRoles.size} dirty roles of shard ${cacheManager.deviousGateway.shardId}" }
                if (removedEmojis.isNotEmpty())
                    logger.info { "${removedEmojis.size} removed emojis of shard ${cacheManager.deviousGateway.shardId}" }
                if (dirtyEmojis.isNotEmpty())
                    logger.info { "${dirtyEmojis.size} dirty emojis of shard ${cacheManager.deviousGateway.shardId}" }
                if (removedChannels.isNotEmpty())
                    logger.info { "${removedChannels.size} removed channels of shard ${cacheManager.deviousGateway.shardId}" }
                if (dirtyChannels.isNotEmpty())
                    logger.info { "${dirtyChannels.size} dirty channels of shard ${cacheManager.deviousGateway.shardId}" }
                if (removedGuilds.isNotEmpty())
                    logger.info { "${removedGuilds.size} removed guilds of shard ${cacheManager.deviousGateway.shardId}" }
                if (dirtyGuilds.isNotEmpty())
                    logger.info { "${dirtyGuilds.size} dirty guilds of shard ${cacheManager.deviousGateway.shardId}" }
                if (removedVoiceStates.isNotEmpty())
                    logger.info { "${removedVoiceStates.size} removed voice states of shard ${cacheManager.deviousGateway.shardId}" }
                if (dirtyVoiceStates.isNotEmpty())
                    logger.info { "${dirtyVoiceStates.size} dirty voice states of shard ${cacheManager.deviousGateway.shardId}" }
                val gatewaySession = cacheManager.gatewaySession?.copy()
                if (gatewaySession != null) {
                    logger.info { "Persisting gateway session with sequence ${gatewaySession.sequence} of shard ${cacheManager.deviousGateway.shardId}" }
                } else {
                    logger.info { "Not persisting gateway session of shard ${cacheManager.deviousGateway.shardId} because it is null" }
                }

                if (gatewaySession != null) {
                    GatewaySessions.upsert(GatewaySessions.id) {
                        it[GatewaySessions.id] = cacheManager.deviousGateway.shardId
                        it[GatewaySessions.sessionId] = gatewaySession.sessionId
                        it[GatewaySessions.resumeGatewayUrl] = gatewaySession.resumeGatewayUrl
                        it[GatewaySessions.sequence] = gatewaySession.sequence
                    }
                }

                if (dirtyUsers.isNotEmpty()) {
                    Users.batchUpsert(dirtyUsers.entries, Users.id) { it, data ->
                        it[Users.id] = data.key.value.toLong()
                        it[Users.data] = Json.encodeToString(data.value)
                    }
                }

                for ((guildId, userId) in removedMembers) {
                    // Sadly we need to issue a separate query for each null member
                    GuildMembers.deleteWhere { GuildMembers.userId eq userId.value.toLong() and (GuildMembers.guildId eq guildId.value.toLong()) }
                }

                if (dirtyMembers.isNotEmpty())
                    GuildMembers.batchUpsert(dirtyMembers.map { it.key to it.value }, GuildMembers.guildId, GuildMembers.userId) { it, data ->
                        it[GuildMembers.guildId] = data.first.guildId.value.toLong()
                        it[GuildMembers.userId] = data.first.userId.value.toLong()
                        it[GuildMembers.data] = Json.encodeToString(data.second)
                    }

                if (removedRoles.isNotEmpty()) {
                    GuildRoles.deleteWhere { GuildRoles.id inList removedRoles.map { it.value.toLong() } }
                }

                if (dirtyRoles.isNotEmpty()) {
                    GuildRoles.batchUpsert(dirtyRoles.entries, GuildRoles.id) { it, data ->
                        it[GuildRoles.id] = data.key.value.toLong()
                        it[GuildRoles.data] = Json.encodeToString(data.value.values)
                    }
                }

                if (removedEmojis.isNotEmpty()) {
                    GuildEmojis.deleteWhere { GuildEmojis.id inList removedEmojis.map { it.value.toLong() } }
                }

                if (dirtyEmojis.isNotEmpty()) {
                    GuildEmojis.batchUpsert(dirtyEmojis.entries, GuildEmojis.id) { it, data ->
                        it[GuildEmojis.id] = data.key.value.toLong()
                        it[GuildEmojis.data] = Json.encodeToString(data.value.values)
                    }
                }

                if (removedChannels.isNotEmpty()) {
                    GuildChannels.deleteWhere { GuildChannels.id inList removedChannels.map { it.value.toLong() } }
                }

                if (dirtyChannels.isNotEmpty()) {
                    GuildChannels.batchUpsert(dirtyChannels.entries, GuildChannels.id) { it, data ->
                        it[GuildChannels.id] = data.key.value.toLong()
                        it[GuildChannels.data] = Json.encodeToString(data.value.values)
                    }
                }

                if (removedGuilds.isNotEmpty()) {
                    Guilds.deleteWhere { Guilds.id inList removedGuilds.map { it.value.toLong() } }
                }

                if (dirtyGuilds.isNotEmpty()) {
                    Guilds.batchUpsert(dirtyGuilds.entries, Guilds.id) { it, data ->
                        it[Guilds.id] = data.key.value.toLong()
                        it[Guilds.data] = Json.encodeToString(data.value)
                    }
                }

                if (removedVoiceStates.isNotEmpty()) {
                    GuildVoiceStates.deleteWhere { GuildVoiceStates.id inList removedVoiceStates.map { it.value.toLong() } }
                }

                if (dirtyVoiceStates.isNotEmpty()) {
                    GuildVoiceStates.batchUpsert(dirtyVoiceStates.entries, GuildVoiceStates.id) { it, data ->
                        it[GuildVoiceStates.id] = data.key.value.toLong()
                        it[GuildVoiceStates.data] = Json.encodeToString(data.value.values)
                    }
                }
            }
        }
        logger.info { "Persisted shard ${cacheManager.deviousGateway.shardId} cache data to database! Took $time" }
    }

    init {
        val scope = CoroutineScope(Dispatchers.IO)
        val pendingQueues = scheduleCoroutineAtFixedRate(scope, 5.seconds) {
            runPendingQueries()
        }

        Runtime.getRuntime().addShutdownHook(
            thread(false) {
                if (!isActive)
                    return@thread

                logger.info { "Shutting down DeviousCacheDatabase of shard ${cacheManager.deviousGateway.shardId}.." }
                logger.info { "Stopping pending queue recurring job of shard ${cacheManager.deviousGateway.shardId}..." }
                pendingQueues.cancel()
                logger.info { "Waiting for all queued actions to be processed of shard ${cacheManager.deviousGateway.shardId}..." }
                val pendingQueuedActions = mutableListOf<Job>()
                queuedActions.drainTo(pendingQueuedActions)
                runBlocking {
                    pendingQueuedActions.joinAll()
                }
                logger.info { "Waiting for all pending queries to be processed of shard ${cacheManager.deviousGateway.shardId}..." }
                runBlocking {
                    runPendingQueries()
                }
                logger.info { "Successfully persisted all data during shutdown of shard ${cacheManager.deviousGateway.shardId}! :3" }
            }
        )
    }

    suspend fun stop() {
        if (!isActive)
            return

        isActive = false

        // Shutdown
        scope.cancel()
        dirtyEntities.users.clear()
        dirtyEntities.members.clear()
        dirtyEntities.roles.clear()
        dirtyEntities.emojis.clear()
        dirtyEntities.voiceStates.clear()
        dirtyEntities.guildChannels.clear()
        dirtyEntities.guilds.clear()

        TransactionManager.closeAndUnregister(database)
    }

    class DirtyEntitiesWrapper {
        // Because we are using a mutex to block access to this, we don't need to worry about ConcurrentModificationExceptions
        val users = mutableMapOf<LightweightSnowflake, DatabaseCacheValue<DeviousUserData>>()
        val members = mutableMapOf<GuildAndUserPair, DatabaseCacheValue<DeviousMemberData>>()
        val roles = mutableMapOf<LightweightSnowflake, DatabaseCacheValue<Map<LightweightSnowflake, DeviousRoleData>>>()
        val emojis = mutableMapOf<LightweightSnowflake, DatabaseCacheValue<Map<LightweightSnowflake, DeviousGuildEmojiData>>>()
        val voiceStates = mutableMapOf<LightweightSnowflake, DatabaseCacheValue<Map<LightweightSnowflake, DeviousVoiceStateData>>>()
        val guildChannels = mutableMapOf<LightweightSnowflake, DatabaseCacheValue<Map<LightweightSnowflake, DeviousChannelData>>>()
        val guilds = mutableMapOf<LightweightSnowflake, DatabaseCacheValue<DeviousGuildDataWrapper>>()

        fun <K, V> filterNull(map: Map<K, DatabaseCacheValue<V>>): Set<K> {
            return map.filterValues { it is DatabaseCacheValue.Null<*> }.keys
        }

        fun <K, V> filterNotNull(map: Map<K, DatabaseCacheValue<V>>): Map<K, V> {
            return (map.filterValues { it is DatabaseCacheValue.Value<*> } as Map<K, DatabaseCacheValue.Value<V>>).mapValues { it.value.data }
        }
    }
}