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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
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
    }

    private val dirtyEntities = DirtyEntitiesWrapper()
    private val mutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.Default)
    private val queuedActions = LinkedBlockingQueue<Job>()

    /**
     * Queues an action to be added to the [DirtyEntitiesWrapper], access synchronized via [mutex].
     */
    fun queue(action: DirtyEntitiesWrapper.() -> (Unit)) {
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
        val dirtyChannels: Map<LightweightSnowflake, DeviousChannelData>
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

            removedChannels = dirtyEntities.filterNull(dirtyEntities.channels)
            dirtyChannels = dirtyEntities.filterNotNull(dirtyEntities.channels)

            removedGuilds = dirtyEntities.filterNull(dirtyEntities.guilds)
            dirtyGuilds = dirtyEntities.filterNotNull(dirtyEntities.guilds)

            removedVoiceStates = dirtyEntities.filterNull(dirtyEntities.voiceStates)
            dirtyVoiceStates = dirtyEntities.filterNotNull(dirtyEntities.voiceStates)

            dirtyEntities.users.clear()
            dirtyEntities.members.clear()
            dirtyEntities.roles.clear()
            dirtyEntities.emojis.clear()
            dirtyEntities.channels.clear()
            dirtyEntities.guilds.clear()
            dirtyEntities.voiceStates.clear()
        }

        val time = measureTime {
            transaction(Dispatchers.IO, database) {
                logger.info { "${dirtyUsers.size} dirty users" }
                logger.info { "${removedMembers.size} removed members" }
                logger.info { "${dirtyMembers.size} dirty members" }
                logger.info { "${removedRoles.size} removed roles" }
                logger.info { "${dirtyRoles.size} dirty roles" }
                logger.info { "${removedEmojis.size} removed emojis" }
                logger.info { "${dirtyEmojis.size} dirty emojis" }
                logger.info { "${removedChannels.size} removed channels" }
                logger.info { "${dirtyChannels.size} dirty channels" }
                logger.info { "${removedGuilds.size} removed guilds" }
                logger.info { "${dirtyGuilds.size} dirty guilds" }
                logger.info { "${removedVoiceStates.size} removed voice states" }
                logger.info { "${dirtyVoiceStates.size} dirty voice states" }
                val gatewaySessions = cacheManager.gatewaySessions.toMap()
                logger.info { "${gatewaySessions.size} gateway sessions" }

                if (gatewaySessions.isNotEmpty()) {
                    GatewaySessions.batchUpsert(gatewaySessions.entries, GatewaySessions.id) { it, data ->
                        it[GatewaySessions.id] = data.key
                        it[GatewaySessions.sessionId] = data.value.sessionId
                        it[GatewaySessions.resumeGatewayUrl] = data.value.resumeGatewayUrl
                        it[GatewaySessions.sequence] = data.value.sequence
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
                        it[GuildRoles.data] = Json.encodeToString(data.value)
                    }
                }

                if (removedEmojis.isNotEmpty()) {
                    GuildEmojis.deleteWhere { GuildEmojis.id inList removedEmojis.map { it.value.toLong() } }
                }

                if (dirtyEmojis.isNotEmpty()) {
                    GuildEmojis.batchUpsert(dirtyEmojis.entries, GuildEmojis.id) { it, data ->
                        it[GuildEmojis.id] = data.key.value.toLong()
                        it[GuildEmojis.data] = Json.encodeToString(data.value)
                    }
                }

                if (removedChannels.isNotEmpty()) {
                    Channels.deleteWhere { Channels.id inList removedChannels.map { it.value.toLong() } }
                }

                if (dirtyChannels.isNotEmpty()) {
                    Channels.batchUpsert(dirtyChannels.entries, Channels.id) { it, data ->
                        it[Channels.id] = data.key.value.toLong()
                        it[Channels.data] = Json.encodeToString(data.value)
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
                        it[GuildVoiceStates.data] = Json.encodeToString(data.value)
                    }
                }
            }
        }
        logger.info { "Persisted cache data to database! Took $time" }
    }

    init {
        val scope = CoroutineScope(Dispatchers.IO)
        val pendingQueues = scheduleCoroutineAtFixedRate(scope, 5.seconds) {
            runPendingQueries()
        }

        Runtime.getRuntime().addShutdownHook(
            thread(false) {
                logger.info { "Shutting down DeviousCacheDatabase..." }
                logger.info { "Stopping pending queue recurring job..." }
                pendingQueues.cancel()
                logger.info { "Waiting for all queued actions to be processed..." }
                val pendingQueuedActions = mutableListOf<Job>()
                queuedActions.drainTo(pendingQueuedActions)
                runBlocking {
                    pendingQueuedActions.joinAll()
                }
                logger.info { "Waiting for all pending queries to be processed..." }
                runBlocking {
                    runPendingQueries()
                }
                logger.info { "Successfully persisted all data during shutdown! :3" }
            }
        )
    }

    class DirtyEntitiesWrapper {
        // Because we are using a mutex to block access to this, we don't need to worry about ConcurrentModificationExceptions
        val users = mutableMapOf<LightweightSnowflake, DatabaseCacheValue<DeviousUserData>>()
        val members = mutableMapOf<GuildAndUserPair, DatabaseCacheValue<DeviousMemberData>>()
        val roles = mutableMapOf<LightweightSnowflake, DatabaseCacheValue<Map<LightweightSnowflake, DeviousRoleData>>>()
        val emojis = mutableMapOf<LightweightSnowflake, DatabaseCacheValue<Map<LightweightSnowflake, DeviousGuildEmojiData>>>()
        val voiceStates = mutableMapOf<LightweightSnowflake, DatabaseCacheValue<Map<LightweightSnowflake, DeviousVoiceStateData>>>()
        val channels = mutableMapOf<LightweightSnowflake, DatabaseCacheValue<DeviousChannelData>>()
        val guilds = mutableMapOf<LightweightSnowflake, DatabaseCacheValue<DeviousGuildDataWrapper>>()

        fun <K, V> filterNull(map: Map<K, DatabaseCacheValue<V>>): Set<K> {
            return map.filterValues { it is DatabaseCacheValue.Null<*> }.keys
        }

        fun <K, V> filterNotNull(map: Map<K, DatabaseCacheValue<V>>): Map<K, V> {
            return (map.filterValues { it is DatabaseCacheValue.Value<*> } as Map<K, DatabaseCacheValue.Value<V>>).mapValues { it.value.data }
        }
    }
}