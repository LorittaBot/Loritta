package net.perfectdreams.loritta.deviouscache.server

import com.github.benmanes.caffeine.cache.Caffeine
import dev.kord.common.entity.Snowflake
import io.ktor.network.tls.certificates.*
import io.ktor.server.engine.*
import io.ktor.server.jetty.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.exposedpowerutils.sql.transaction
import net.perfectdreams.loritta.deviouscache.data.*
import net.perfectdreams.loritta.deviouscache.server.processors.Processors
import net.perfectdreams.loritta.deviouscache.server.routes.PostRpcRoute
import net.perfectdreams.loritta.deviouscache.server.tables.*
import net.perfectdreams.loritta.deviouscache.server.utils.*
import net.perfectdreams.loritta.deviouscache.server.utils.config.BaseConfig
import net.perfectdreams.loritta.deviouscache.server.utils.extensions.batchUpsert
import net.perfectdreams.loritta.deviouscache.server.utils.extensions.upsert
import org.jetbrains.exposed.sql.*
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class DeviousCache(val config: BaseConfig, val database: Database) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val users = ConcurrentHashMap<Snowflake, DeviousUserData>()
    val channels = ConcurrentHashMap<Snowflake, DeviousChannelData>()
    val guilds = ConcurrentHashMap<Snowflake, DeviousGuildDataWrapper>()
    val emotes = ConcurrentHashMap<Snowflake, Map<Snowflake, DeviousGuildEmojiData>>()
    val roles = ConcurrentHashMap<Snowflake, Map<Snowflake, DeviousRoleData>>()
    val members = ConcurrentHashMap<Snowflake, Map<Snowflake, DeviousMemberData>>()
    val voiceStates = ConcurrentHashMap<Snowflake, Map<Snowflake, DeviousVoiceStateData>>()

    val gatewaySessions = ConcurrentHashMap<Int, DeviousGatewaySession>()

    val miscellaneousData = ConcurrentHashMap<String, String>()
    val miscellaneousDataMutex = ConcurrentHashMap<String, Mutex>()

    val loginBucketsLoggingInStatus = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build<Int, String>()
        .asMap()
    val loginBucketMutex = Mutex()

    // A mutex, kind of
    private val entityPersistenceModificationMutex = MutableStateFlow<CacheEntityStatus>(CacheEntityStatus.OK)

    // Entity specific mutexes
    val mutexes = ConcurrentHashMap<EntityKey, Mutex>()

    internal val dirtyUsers = ConcurrentHashMap.newKeySet<Snowflake>()
    internal val dirtyGuilds = ConcurrentHashMap.newKeySet<Snowflake>()
    internal val dirtyChannels = ConcurrentHashMap.newKeySet<Snowflake>()
    internal val dirtyMembers = ConcurrentHashMap.newKeySet<GuildAndUserPair>()
    internal val dirtyEmojis = ConcurrentHashMap.newKeySet<Snowflake>()
    internal val dirtyRoles = ConcurrentHashMap.newKeySet<Snowflake>()
    internal val dirtyVoiceStates = ConcurrentHashMap.newKeySet<Snowflake>()

    private val routes = listOf(PostRpcRoute(this))

    val processors = Processors(this)

    @OptIn(ExperimentalTime::class)
    fun start() {
        logger.info { "Creating tables..." }
        runBlocking {
            transaction(Dispatchers.IO, database) {
                SchemaUtils.createMissingTablesAndColumns(
                    Users,
                    Guilds,
                    Channels,
                    GuildMembers,
                    GuildRoles,
                    GuildEmojis,
                    GuildVoiceStates,
                    MiscellaneousData,
                    GatewaySessions
                )
            }
        }

        // Starting system...
        logger.info { "Loading Devious Cache data... Hang tight!" }

        runBlocking {
            val duration = measureTime {
                transaction(Dispatchers.IO, database) {
                    logger.info { "Loading users..." }
                    Users.selectAll()
                        .forEach {
                            users[Snowflake(it[Users.id])] = Json.decodeFromString(it[Users.data])
                        }

                    logger.info { "Loading guilds..." }
                    Guilds.selectAll()
                        .forEach {
                            guilds[Snowflake(it[Guilds.id])] = Json.decodeFromString(it[Guilds.data])
                        }

                    logger.info { "Loading members..." }
                    val mutableMaps = mutableMapOf<Snowflake, MutableMap<Snowflake, DeviousMemberData>>()
                    GuildMembers.selectAll()
                        .forEach {
                            val guildMap = mutableMaps.getOrPut(Snowflake(it[GuildMembers.guildId])) { mutableMapOf() }
                            guildMap[Snowflake(it[GuildMembers.userId])] = Json.decodeFromString(it[GuildMembers.data])
                        }

                    members.putAll(mutableMaps)

                    logger.info { "Loading channels..." }
                    Channels.selectAll()
                        .forEach {
                            channels[Snowflake(it[Channels.id])] = Json.decodeFromString(it[Channels.data])
                        }

                    logger.info { "Loading roles..." }
                    GuildRoles.selectAll()
                        .forEach {
                            roles[Snowflake(it[GuildRoles.id])] = Json.decodeFromString(it[GuildRoles.data])
                        }

                    logger.info { "Loading emojis..." }
                    GuildEmojis.selectAll()
                        .forEach {
                            emotes[Snowflake(it[GuildEmojis.id])] = Json.decodeFromString(it[GuildEmojis.data])
                        }

                    logger.info { "Loading voice states..." }
                    GuildVoiceStates.selectAll()
                        .forEach {
                            voiceStates[Snowflake(it[GuildVoiceStates.id])] = Json.decodeFromString(it[GuildVoiceStates.data])
                        }

                    logger.info { "Loading gateway sessions..." }
                    GatewaySessions.selectAll()
                        .forEach {
                            gatewaySessions[it[GatewaySessions.id]] = DeviousGatewaySession(
                                it[GatewaySessions.sessionId],
                                it[GatewaySessions.resumeGatewayUrl],
                                it[GatewaySessions.sequence],
                            )
                        }

                    logger.info { "Loading miscellaneous data..." }
                    MiscellaneousData.selectAll()
                        .forEach {
                            miscellaneousData[it[MiscellaneousData.id]] = it[MiscellaneousData.data]
                        }
                }
            }

            logger.info { "Data successfully loaded! Took $duration" }
        }

        // Create a self-signed certificate
        // We don't really care about "security" here because the service should not be exposed to the public
        val keyStoreFile = File.createTempFile("deviouscache-keystore", ".jks")
        val keyStorePassword = "foobar"
        val keyStore = generateCertificate(
            file = keyStoreFile,
            keyAlias = "sampleAlias",
            keyPassword = keyStorePassword,
            jksPassword = keyStorePassword
        )

        scheduleCoroutineAtFixedRate(
            CoroutineScope(Dispatchers.Default),
            5.seconds
        ) {
            val mb = 1024 * 1024
            val runtime = Runtime.getRuntime()

            logger.info { "Cache Stats:" }
            logger.info { "Users: ${users.size} users" }
            logger.info { "Guilds: ${guilds.size} guilds" }
            logger.info { "Channels: ${channels.size} channels" }
            logger.info { "Guild Members: ${members.size} members (${members.values.sumOf { it.size }} total)" }
            logger.info { "Guild Roles: ${roles.size} guild roles (${roles.values.sumOf { it.size }} total)" }
            logger.info { "Guild Emojis: ${emotes.size} guild emojis (${emotes.values.sumOf { it.size }} total)" }
            logger.info { "Guild Voice States: ${voiceStates.size} guild voice states (${voiceStates.values.sumOf { it.size }} total)" }

            logger.info { "Used Memory: ${(runtime.totalMemory() - runtime.freeMemory()) / mb}MiB" }
            logger.info { "Free Memory: ${runtime.freeMemory() / mb}MiB" }
            logger.info { "Total Memory: ${runtime.totalMemory() / mb}MiB" }
            logger.info { "Max Memory: ${runtime.maxMemory() / mb}MiB" }
        }

        println(config.persistenceDelay)

        scheduleCoroutineAtFixedRate(
            CoroutineScope(Dispatchers.IO),
            config.persistenceDelay.milliseconds,
            config.persistenceDelay.milliseconds
        ) {
            persistData("periodic save")
        }

        Runtime.getRuntime().addShutdownHook(
            thread(false) {
                runBlocking {
                    persistData("shutdown")
                }
            }
        )

        val server = embeddedServer(
            Jetty,
            environment = applicationEngineEnvironment {
                log = LoggerFactory.getLogger("ktor.application")

                // Enables SSL, for this we need our own
                sslConnector(
                    keyStore = keyStore,
                    keyAlias = keyStorePassword,
                    keyStorePassword = { keyStorePassword.toCharArray() },
                    privateKeyPassword = { keyStorePassword.toCharArray() }
                ) {
                    host = this@DeviousCache.config.host
                    port = 8080
                }

                module {
                    routing {
                        for (route in routes) {
                            route.register(this)
                        }
                    }
                }
            }
        )
        server.start(true)
    }

    /**
     * Locks the [entityKeys] for manipulation.
     *
     * The mutexes are locked on the following order:
     * * Guild
     * * Channel
     * * User
     * and the IDs are sorted per category from smallest to largest.
     *
     * This order is necessary to avoid deadlocking when two coroutines invoke withLock at the same time!
     */
    suspend inline fun <T> withLock(vararg entityKeys: EntityKey, action: () -> (T)): T {
        val sortedEntityKeys = mutableListOf<EntityKey>()

        for (entityKey in entityKeys) {
            if (entityKey is GuildKey)
                sortedEntityKeys.add(entityKey)
        }

        for (entityKey in entityKeys) {
            if (entityKey is ChannelKey)
                sortedEntityKeys.add(entityKey)
        }

        for (entityKey in entityKeys) {
            if (entityKey is UserKey)
                sortedEntityKeys.add(entityKey)
        }

        val mutexesToBeLocked = sortedEntityKeys
            .sortedBy { it.id }
            .map { mutexes.getOrPut(it) { Mutex() } }

        for (mutex in mutexesToBeLocked) {
            mutex.lock()
        }

        try {
            return action.invoke()
        } finally {
            for (mutex in mutexesToBeLocked) {
                mutex.unlock()
            }
        }
    }

    suspend fun awaitForEntityPersistenceModificationMutex() {
        // Wait until it is ok before proceeding
        entityPersistenceModificationMutex
            .filter {
                it == CacheEntityStatus.OK
            }
            .first()
    }

    @OptIn(ExperimentalTime::class)
    suspend fun persistData(reason: String) {
        logger.info { "Persisting data... - $reason" }

        val duration = measureTime {
            // We are going to lock the global mutex, just so we can create a clone of the guild/user/channel/etc stuff
            // TODO: Create a better global mutex, maybe a stateflow?
            // globalMutex.lock()

            // Create all user/member/guilds keys
            // TODO: Mutex, we need to lock the dirty list for manipulation
            logger.info { "Locking the entity persistence modification mutex..." }
            entityPersistenceModificationMutex.value = CacheEntityStatus.WAIT

            // TODO: Implement all keys
            val keys = mutableSetOf<EntityKey>()
            keys.addAll(dirtyGuilds.map { GuildKey(it) })
            keys.addAll(dirtyUsers.map { UserKey(it) })
            keys.addAll(dirtyMembers.map { GuildKey(it.guildId) })
            keys.addAll(dirtyMembers.map { UserKey(it.userId) })
            keys.addAll(dirtyChannels.map { ChannelKey(it) })
            keys.addAll(dirtyEmojis.map { GuildKey(it) })
            keys.addAll(dirtyRoles.map { GuildKey(it) })
            keys.addAll(dirtyVoiceStates.map { GuildKey(it) })

            val currentDirtyUsers = dirtyUsers.toSet()
            val currentDirtyGuilds = dirtyGuilds.toSet()
            val currentDirtyMembers = dirtyMembers.toSet()
            val currentDirtyChannels = dirtyChannels.toSet()
            val currentDirtyEmojis = dirtyEmojis.toSet()
            val currentDirtyRoles = dirtyRoles.toSet()
            val currentDirtyVoiceStates = dirtyVoiceStates.toSet()

            // Get all dirty data
            val dirtyUsersData = mutableMapOf<Snowflake, DeviousUserData?>()
            val dirtyGuildsData = mutableMapOf<Snowflake, DeviousGuildDataWrapper?>()
            val dirtyMembersData = mutableMapOf<GuildAndUserPair, DeviousMemberData?>()
            val dirtyChannelsData = mutableMapOf<Snowflake, DeviousChannelData?>()
            val dirtyEmojisData = mutableMapOf<Snowflake, Map<Snowflake, DeviousGuildEmojiData>?>()
            val dirtyRolesData = mutableMapOf<Snowflake, Map<Snowflake, DeviousRoleData>?>()
            val dirtyVoiceStatesData = mutableMapOf<Snowflake, Map<Snowflake, DeviousVoiceStateData>?>()

            withLock(*keys.toTypedArray()) {
                for (dirtyUserId in currentDirtyUsers) {
                    dirtyUsersData[dirtyUserId] = users[dirtyUserId]
                }

                for (dirtyGuildId in currentDirtyGuilds) {
                    dirtyGuildsData[dirtyGuildId] = guilds[dirtyGuildId]
                }

                for ((guildId, dirtyUserId) in currentDirtyMembers) {
                    val data = members[guildId] ?: continue
                    dirtyMembersData[GuildAndUserPair(guildId, dirtyUserId)] = data[dirtyUserId]
                }

                for (channelId in currentDirtyChannels) {
                    dirtyChannelsData[channelId] = channels[channelId]
                }

                for (dirtyGuildId in currentDirtyRoles) {
                    dirtyRolesData[dirtyGuildId] = roles[dirtyGuildId]
                }

                for (dirtyGuildId in currentDirtyEmojis) {
                    dirtyEmojisData[dirtyGuildId] = emotes[dirtyGuildId]
                }

                for (dirtyGuildId in currentDirtyVoiceStates) {
                    dirtyVoiceStatesData[dirtyGuildId] = voiceStates[dirtyGuildId]
                }
            }

            // Now that we have persisted all changes...

            // Clear the dirty status
            dirtyUsers.clear()
            dirtyGuilds.clear()
            dirtyMembers.clear()
            dirtyChannels.clear()
            dirtyEmojis.clear()
            dirtyRoles.clear()
            dirtyVoiceStates.clear()

            // And let requests manipulate the cache again!
            entityPersistenceModificationMutex.value = CacheEntityStatus.OK

            logger.info { "Unlocked the entity persistence modification mutex!" }
            logger.info { "Users to be persisted: ${currentDirtyUsers.size}" }
            logger.info { "Guilds to be persisted: ${currentDirtyGuilds.size}" }
            logger.info { "Members to be persisted: ${currentDirtyMembers.size}" }
            logger.info { "Channels to be persisted: ${currentDirtyChannels.size}" }
            logger.info { "Guild Roles to be persisted: ${currentDirtyRoles.size}" }
            logger.info { "Guild Emojis to be persisted: ${currentDirtyEmojis.size}" }
            logger.info { "Guild Voice States to be persisted: ${currentDirtyVoiceStates.size}" }
            logger.info { "Miscellaneous Data to be persisted: ${miscellaneousData.size}" }
            logger.info { "Gateway Sessions to be persisted: ${gatewaySessions.size}" }

            // Persist the dirty data to the database
            transaction(Dispatchers.IO, database) {
                // Always upsert the miscellaneous data
                // TODO: Implement dirty data support?
                for (data in miscellaneousData) {
                    MiscellaneousData.upsert(MiscellaneousData.id) {
                        it[MiscellaneousData.id] = data.key
                        it[MiscellaneousData.data] = data.value
                    }
                }

                for ((shardId, data) in gatewaySessions) {
                    GatewaySessions.upsert(GatewaySessions.id) {
                        it[GatewaySessions.id] = shardId
                        it[GatewaySessions.sessionId] = data.sessionId
                        it[GatewaySessions.resumeGatewayUrl] = data.resumeGatewayUrl
                        it[GatewaySessions.sequence] = data.sequence
                    }
                }

                val nullUsersData = dirtyUsersData.filterValues { it == null }
                val nonNullUsersData = dirtyUsersData.filterValues { it != null }

                Users.deleteWhere { Users.id inList nullUsersData.map { it.key.value.toLong() } }

                if (nonNullUsersData.isNotEmpty())
                    Users.batchUpsert(nonNullUsersData.map { it.key to it.value }, Users.id) { it, data ->
                        it[Users.id] = data.first.value.toLong()
                        it[Users.data] = Json.encodeToString(data.second)
                    }

                val nullGuildsData = dirtyGuildsData.filterValues { it == null }
                val nonNullGuildsData = dirtyGuildsData.filterValues { it != null }

                Guilds.deleteWhere { Guilds.id inList nullGuildsData.map { it.key.value.toLong() } }

                if (nonNullGuildsData.isNotEmpty()) {
                    Guilds.batchUpsert(nonNullGuildsData.map { it.key to it.value }, Users.id) { it, data ->
                        it[Guilds.id] = data.first.value.toLong()
                        it[Guilds.data] = Json.encodeToString(data.second)
                    }
                }

                val nullMembersData = dirtyMembersData.filterValues { it == null }
                val nonNullMembersData = dirtyMembersData.filterValues { it != null }
                for ((guildAndUser, _) in nullMembersData) {
                    // Sadly we need to issue a separate query for each null member
                    GuildMembers.deleteWhere { GuildMembers.userId eq guildAndUser.userId.value.toLong() and (GuildMembers.guildId eq guildAndUser.guildId.value.toLong()) }
                }

                if (nonNullMembersData.isNotEmpty())
                    GuildMembers.batchUpsert(nonNullMembersData.map { it.key to it.value }, GuildMembers.guildId, GuildMembers.userId) { it, data ->
                        it[GuildMembers.guildId] = data.first.guildId.value.toLong()
                        it[GuildMembers.userId] = data.first.userId.value.toLong()
                        it[GuildMembers.data] = Json.encodeToString(data.second)
                    }

                val nullChannelsData = dirtyChannelsData.filterValues { it == null }
                val nonNullChannelsData = dirtyChannelsData.filterValues { it != null }

                Channels.deleteWhere { Users.id inList nullChannelsData.map { it.key.value.toLong() } }

                if (nonNullChannelsData.isNotEmpty())
                    Channels.batchUpsert(nonNullChannelsData.map { it.key to it.value }, Channels.id) { it, data ->
                        it[Channels.id] = data.first.value.toLong()
                        it[Channels.data] = Json.encodeToString(data.second)
                    }

                val nullRolesData = dirtyRolesData.filterValues { it == null }
                val nonNullRolesData = dirtyRolesData.filterValues { it != null }

                GuildRoles.deleteWhere { GuildRoles.id inList nullRolesData.map { it.key.value.toLong() } }

                if (nonNullRolesData.isNotEmpty())
                    GuildRoles.batchUpsert(nonNullRolesData.map { it.key to it.value }, GuildRoles.id) { it, data ->
                        it[GuildRoles.id] = data.first.value.toLong()
                        it[GuildRoles.data] = Json.encodeToString(data.second)
                    }

                val nullEmojisData = dirtyEmojisData.filterValues { it == null }
                val nonNullEmojisData = dirtyEmojisData.filterValues { it != null }

                GuildEmojis.deleteWhere { GuildEmojis.id inList nullEmojisData.map { it.key.value.toLong() } }

                if (nonNullEmojisData.isNotEmpty())
                    GuildEmojis.batchUpsert(nonNullEmojisData.map { it.key to it.value }, GuildEmojis.id) { it, data ->
                        it[GuildEmojis.id] = data.first.value.toLong()
                        it[GuildEmojis.data] = Json.encodeToString(data.second)
                    }

                val nullVoiceStatesData = dirtyVoiceStatesData.filterValues { it == null }
                val nonNullVoiceStatesData = dirtyVoiceStatesData.filterValues { it != null }

                GuildVoiceStates.deleteWhere { GuildVoiceStates.id inList nullVoiceStatesData.map { it.key.value.toLong() } }

                if (nonNullVoiceStatesData.isNotEmpty())
                    GuildVoiceStates.batchUpsert(nonNullVoiceStatesData.map { it.key to it.value }, GuildVoiceStates.id) { it, data ->
                        it[GuildVoiceStates.id] = data.first.value.toLong()
                        it[GuildVoiceStates.data] = Json.encodeToString(data.second)
                    }
            }
        }

        logger.info { "Data successfully persisted! Took $duration" }
    }
}