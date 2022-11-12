package net.perfectdreams.loritta.morenitta

import dev.kord.common.entity.DiscordShard
import dev.kord.common.entity.PresenceStatus
import dev.kord.gateway.*
import dev.kord.gateway.retry.LinearRetry
import it.unimi.dsi.fastutil.longs.Long2LongMaps
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.debug.DebugProbes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import mu.KotlinLogging
import net.perfectdreams.exposedpowerutils.sql.transaction
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.InteractionsMetrics
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.common.locale.LocaleManager
import net.perfectdreams.loritta.common.locale.LorittaLanguageManager
import net.perfectdreams.loritta.common.utils.HostnameUtils
import net.perfectdreams.loritta.deviouscache.data.*
import net.perfectdreams.loritta.deviousfun.DeviousShard
import net.perfectdreams.loritta.deviousfun.cache.DeviousCacheDatabase
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway
import net.perfectdreams.loritta.deviousfun.gateway.EventsChannel
import net.perfectdreams.loritta.deviousfun.gateway.ParallelIdentifyRateLimiter
import net.perfectdreams.loritta.deviousfun.tables.*
import net.perfectdreams.loritta.deviousfun.utils.CacheEntityMaps
import net.perfectdreams.loritta.deviousfun.utils.DeviousGuildDataWrapper
import net.perfectdreams.loritta.deviousfun.utils.SnowflakeMap
import net.perfectdreams.loritta.morenitta.utils.config.BaseConfig
import net.perfectdreams.loritta.morenitta.utils.config.LorittaConfig
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

/**
 * Loritta's Launcher
 *
 * @author MrPowerGamerBR
 */
object LorittaLauncher {
    private val logger = KotlinLogging.logger {}
    private val OFFSET_SIZE = 512
    private val gatewayScope = CoroutineScope(Dispatchers.Default)

    @OptIn(ExperimentalTime::class)
    @JvmStatic
    fun main(args: Array<String>) {
        // https://github.com/JetBrains/Exposed/issues/1356
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        installCoroutinesDebugProbes()

        // Speeds up image loading/writing/etc
        // https://stackoverflow.com/a/44170254/7271796
        ImageIO.setUseCache(false)

        val configurationFile = File(System.getProperty("conf") ?: "./loritta.conf")

        if (!configurationFile.exists()) {
            println("Welcome to Loritta Morenitta! :3")
            println("")
            println("I want to make the world a better place... helping people, making them laugh... I hope I succeed!")
            println("")
            println("Before we start, you need to configure me!")
            println("I created a file named \"loritta.conf\", there you can configure a lot of things and stuff related to me, open it on your favorite text editor and change it!")
            println("")
            println("After configuring the file, run me again!")

            copyFromJar("/loritta.conf", "./loritta.conf")
            copyFromJar("/emotes.conf", "./emotes.conf")

            System.exit(1)
            return
        }

        val config = readConfigurationFromFile<BaseConfig>(configurationFile)
        logger.info { "Loaded Loritta's configuration file" }

        val clusterId = if (config.loritta.clusters.getClusterIdFromHostname) {
            val hostname = HostnameUtils.getHostname()
            hostname.substringAfterLast("-").toIntOrNull()
                ?: error("Clusters are enabled, but I couldn't get the Cluster ID from the hostname!")
        } else {
            config.loritta.clusters.clusterIdOverride ?: 1
        }

        logger.info { "Loritta's Cluster ID: $clusterId" }

        // Used for Logback
        System.setProperty(
            "cluster.name",
            config.loritta.clusters.instances.first { it.id == clusterId }.getUserAgent(config.loritta.environment)
        )

        InteractionsMetrics.registerJFRExports()
        InteractionsMetrics.registerInteractions()

        logger.info { "Registered Prometheus Metrics" }

        logger.info { "Loading languages..." }
        val languageManager = LorittaLanguageManager(LorittaBot::class)
        val localeManager = LocaleManager(LorittaBot::class).also { it.loadLocales() }

        val services = Pudding.createPostgreSQLPudding(
            config.loritta.pudding.address,
            config.loritta.pudding.database,
            config.loritta.pudding.username,
            config.loritta.pudding.password
        )
        services.setupShutdownHook()

        logger.info { "Started Pudding client!" }

        // Start connection bootstrapping fun stuff!
        logger.info { "Bootstrapping gateway connections..." }
        val lorittaCluster = config.loritta.clusters.instances.first { it.id == clusterId }
        val minShard = lorittaCluster.minShard
        val maxShard = lorittaCluster.maxShard

        val identifyLock = Mutex()
        // Lock identifies, fresh started shards won't connect nor identify to Discord until we unlock
        // We are using tryLock mostly because we aren't in a runBlocking call, but because we are locking right after the Mutex creation, it won't cause any
        // issues ;)
        identifyLock.tryLock()

        runBlocking {
            val jobs = (minShard..maxShard).map { shardId ->
                async(Dispatchers.IO) {
                    val status: MutableStateFlow<DeviousGateway.Status> = MutableStateFlow(DeviousGateway.Status.UNKNOWN)

                    gatewayScope.launch {
                        status.collect {
                            logger.info { "Shard $shardId new status is $it" }
                        }
                    }

                    val identifyRateLimiter = ParallelIdentifyRateLimiter(
                        shardId,
                        shardId % config.loritta.discord.maxConcurrency,
                        config.loritta.discord.maxConcurrency,
                        services,
                        status,
                        identifyLock
                    )

                    // Load database
                    val database = DeviousCacheDatabase.createCacheDatabase(shardId)

                    // First we will try getting a gateway session
                    val gatewaySession = transaction(Dispatchers.IO, database) {
                        // Only get the gateway session if all cache tables exist
                        if (DeviousCacheDatabase.cacheTables.all { it.exists() }) {
                            GatewaySessions.select { GatewaySessions.id eq shardId }
                                .limit(1)
                                .firstOrNull()
                                ?.let {
                                    DeviousGatewaySession(
                                        it[GatewaySessions.sessionId],
                                        it[GatewaySessions.resumeGatewayUrl],
                                        it[GatewaySessions.sequence],
                                    )
                                }
                        } else {
                            null
                        }
                    }

                    if (gatewaySession != null) {
                        // Now, with the gateway session, we already can do some fun stuff with it
                        val gateway = DefaultGateway {
                            // The default reconnectRetry is 10, but let's try reconnecting indefinitely (well, kind of, it will try reconnecting MAX_VALUE times)
                            this.reconnectRetry = LinearRetry(2.seconds, 20.seconds, Int.MAX_VALUE)

                            this.identifyRateLimiter = identifyRateLimiter
                        }

                        logger.info { "Resuming shard $shardId... Hang tight!" }

                        val result = Channel<Boolean>()

                        val receivedEvents = EventsChannel()
                        val receivedEventsJob = gateway.launch {
                            gateway.events.collect {
                                // Collect events to our channel
                                // This job should NOT be cancelled!
                                receivedEvents.send(it)
                            }
                        }

                        val collectJob = gateway.launch {
                            gateway.events.collect {
                                // Track if it was a successful resume or not
                                if (it is Close)
                                    result.send(false)
                                else if (it is Resumed)
                                    result.send(true)
                            }
                        }

                        val gatewayJob = gatewayScope.launch {
                            // If successful, the Gateway will send the missed events in order, finishing with a Resumed event to signal event replay has finished and that all subsequent events will be new.
                            gateway.resume(
                                config.loritta.discord.token,
                                GatewaySession(
                                    gatewaySession.sessionId,
                                    gatewaySession.resumeGatewayUrl,
                                    gatewaySession.sequence
                                ),
                                createGatewayConfiguration(config.loritta.discord.activity.name, lorittaCluster, shardId, config.loritta.discord.maxShards)
                            )
                        }

                        val hasResumed = result.receive()

                        logger.info { "Has shard $shardId resumed? $hasResumed" }

                        if (hasResumed) {
                            // If we successfully resumed, then load the data from the cache!
                            // Events will be stored on the receivedEvents list for now, so we don't need to worry about it
                            collectJob.cancel()
                            result.cancel()

                            // Let's also indicate that we successfully resumed on Loritta's status, this will be changed after receiving Ready
                            gateway.editPresence {
                                this.status = PresenceStatus.DoNotDisturb
                                playing(DeviousShard.createActivityTextWithShardAndClusterId("\uD83C\uDF6E Loritta is loading... Hang tight!", lorittaCluster, shardId))
                            }

                            val (cacheEntityMaps, duration) = measureTimedValue {
                                transaction(Dispatchers.IO, database) {
                                    logger.info { "Loading users for shard $shardId..." }
                                    val userCount = Users.selectAll().count()
                                    logger.info { "User Count for shard $shardId: $userCount" }
                                    val users = SnowflakeMap<DeviousUserData>(userCount.toInt())

                                    Users.selectAll()
                                        .forEach {
                                            users[LightweightSnowflake(it[Users.id])] =
                                                Json.decodeFromString(it[Users.data])
                                        }

                                    logger.info { "Loading guilds for shard $shardId..." }
                                    val guildCount = Guilds.selectAll().count()
                                    logger.info { "Guild Count for shard $shardId: $guildCount" }
                                    val guilds = SnowflakeMap<DeviousGuildDataWrapper>(guildCount.toInt())

                                    Guilds.selectAll()
                                        .forEach {
                                            guilds[LightweightSnowflake(it[Guilds.id])] =
                                                Json.decodeFromString(it[Guilds.data])
                                        }

                                    logger.info { "Loading members for shard $shardId..." }
                                    val mutableMaps = SnowflakeMap<SnowflakeMap<DeviousMemberData>>(guildCount.toInt())
                                    GuildMembers.selectAll()
                                        .forEach {
                                            val guildMap =
                                                mutableMaps.getOrPut(LightweightSnowflake(it[GuildMembers.guildId])) {
                                                    SnowflakeMap(0)
                                                }
                                            guildMap[LightweightSnowflake(it[GuildMembers.userId])] =
                                                Json.decodeFromString(it[GuildMembers.data])
                                        }

                                    val members = SnowflakeMap<SnowflakeMap<DeviousMemberData>>(mutableMaps.size)
                                    for (map in mutableMaps.backedMap) {
                                        members.backedMap[map.key] = map.value
                                    }

                                    logger.info { "Loading channels for shard $shardId..." }
                                    val channelCount = GuildChannels.selectAll().count()
                                    logger.info { "Channel Count for shard $shardId: $channelCount" }
                                    val guildChannels =
                                        SnowflakeMap<SnowflakeMap<DeviousChannelData>>(channelCount.toInt())
                                    // The expected value for this map isn't *really* correct, but it doesn't need to be correct
                                    // In *theory*, the amount of channels is around ~35x the size of guilds, at least according to calculations
                                    // Loaded Guilds: 116736
                                    // Loaded Channels: 3864943
                                    val channelsToGuilds =
                                        Long2LongMaps.synchronize(Long2LongOpenHashMap((channelCount * 35).toInt()))

                                    // Loading all the data without a limit causes a lot of GC pressure, so we will load the data in steps to reduce the pressure
                                    // (Exposed has a "selectAllBatched" but it doesn't work because it requires a auto incrementing column)
                                    //
                                    // Using jdbc's getBinaryStream/getAsciiStream doesn't seem to give a performance boost, in fact, it actually takes *more* time to deserialize with kotlinx.serialization!
                                    fun Table.selectAllForEachWithOffset(
                                        tableSize: Int,
                                        offsetSize: Int,
                                        action: (ResultRow) -> (Unit)
                                    ) {
                                        for (offset in 0 until tableSize step offsetSize) {
                                            this.selectAll()
                                                .fetchSize(offsetSize) // Doesn't really help since we are using SQLite
                                                .limit(offsetSize, offset.toLong())
                                                .forEach(action)
                                            logger.info { "Progress for shard $shardId: ${(offset / tableSize.toDouble()) * 100}%" }
                                        }
                                    }

                                    GuildChannels.selectAllForEachWithOffset(channelCount.toInt(), OFFSET_SIZE) {
                                        val guildId = LightweightSnowflake(it[GuildChannels.id])

                                        val guildChannelsData =
                                            Json.decodeFromString<List<DeviousChannelData>>(it[GuildChannels.data])

                                        val channelsOfThisGuild =
                                            SnowflakeMap<DeviousChannelData>(guildChannelsData.size)

                                        for (guildChannel in guildChannelsData) {
                                            channelsOfThisGuild[guildChannel.id] = guildChannel
                                            channelsToGuilds[guildChannel.id.value.toLong()] = guildId.value.toLong()
                                        }

                                        guildChannels[LightweightSnowflake(it[GuildChannels.id])] = channelsOfThisGuild
                                    }

                                    logger.info { "Loading roles for shard $shardId..." }
                                    val rolesCount = GuildRoles.selectAll().count()
                                    logger.info { "Roles Count for shard $shardId: $rolesCount" }
                                    val roles = SnowflakeMap<SnowflakeMap<DeviousRoleData>>(rolesCount.toInt())
                                    GuildRoles.selectAllForEachWithOffset(rolesCount.toInt(), OFFSET_SIZE) {
                                        val guildRolesData =
                                            Json.decodeFromString<List<DeviousRoleData>>(it[GuildRoles.data])
                                        val guildRolesMap = SnowflakeMap<DeviousRoleData>(guildRolesData.size)
                                        for (guildRoleData in guildRolesData) {
                                            guildRolesMap[guildRoleData.id] = guildRoleData
                                        }
                                        roles[LightweightSnowflake(it[GuildRoles.id])] = guildRolesMap
                                    }

                                    logger.info { "Loading emojis for shard $shardId..." }
                                    val emojisCount = GuildEmojis.selectAll().count()
                                    logger.info { "Emojis Count for shard $shardId: $rolesCount" }
                                    val emotes = SnowflakeMap<SnowflakeMap<DeviousGuildEmojiData>>(emojisCount.toInt())
                                    GuildEmojis.selectAllForEachWithOffset(emojisCount.toInt(), OFFSET_SIZE) {
                                        val guildEmojisData =
                                            Json.decodeFromString<List<DeviousGuildEmojiData>>(it[GuildEmojis.data])
                                        val guildEmojisMap = SnowflakeMap<DeviousGuildEmojiData>(guildEmojisData.size)
                                        for (guildEmojiData in guildEmojisData) {
                                            guildEmojisMap[guildEmojiData.id] = guildEmojiData
                                        }
                                        emotes[LightweightSnowflake(it[GuildEmojis.id])] = guildEmojisMap
                                    }

                                    logger.info { "Loading voice states for shard $shardId..." }
                                    val voiceStatesCount = GuildVoiceStates.selectAll().count()
                                    logger.info { "Voice States Count for shard $shardId: $voiceStatesCount" }
                                    val voiceStates =
                                        SnowflakeMap<SnowflakeMap<DeviousVoiceStateData>>(voiceStatesCount.toInt())
                                    GuildVoiceStates.selectAllForEachWithOffset(voiceStatesCount.toInt(), OFFSET_SIZE) {
                                        val guildVoiceStatesData =
                                            Json.decodeFromString<List<DeviousVoiceStateData>>(it[GuildVoiceStates.data])
                                        val guildVoiceStatesMap =
                                            SnowflakeMap<DeviousVoiceStateData>(guildVoiceStatesData.size)
                                        for (guildVoiceState in guildVoiceStatesData) {
                                            guildVoiceStatesMap[guildVoiceState.userId] = guildVoiceState
                                        }
                                        voiceStates[LightweightSnowflake(it[GuildVoiceStates.id])] = guildVoiceStatesMap
                                    }

                                    CacheEntityMaps(
                                        users,
                                        guilds,
                                        guildChannels,
                                        channelsToGuilds,
                                        emotes,
                                        roles,
                                        members,
                                        voiceStates
                                    )
                                }
                            }

                            logger.info { "Data successfully loaded for shard $shardId! Took $duration" }

                            GatewayBootstrapResult.ResumedGateway(
                                shardId,
                                gateway,
                                database,
                                cacheEntityMaps,
                                gatewaySession,
                                identifyRateLimiter,
                                status,
                                receivedEventsJob,
                                receivedEvents
                            )
                        } else {
                            // Failed to resume the connection, let's start from scratch!
                            // Detaching the gateway cancel the coroutine scope automatically, so we don't need to cancel the jobs manually
                            gateway.detach()
                            result.cancel()
                            receivedEvents.close()

                            // Close the open database
                            TransactionManager.closeAndUnregister(database)

                            GatewayBootstrapResult.FailedToResume(shardId, identifyRateLimiter, status)
                        }
                    } else GatewayBootstrapResult.FailedToResume(shardId, identifyRateLimiter, status)
                }
            }

            // To avoid a lot of GC pressure, we will initialize the "from scratch" gateway connections AFTER all gateways are successfully resumed
            // This should return after all resumed gateway connections are up and running
            val (gatewayResults, gatewayResultsDuration) = measureTimedValue {
                jobs.awaitAll()
            }

            logger.info { "Gateway Results (Took $gatewayResultsDuration): ${gatewayResults.count { it is GatewayBootstrapResult.ResumedGateway }} shards were resumed, ${gatewayResults.count { it is GatewayBootstrapResult.FailedToResume }} shards needs to start from scratch" }
            if (gatewayResults.count { it is GatewayBootstrapResult.ResumedGateway } == (maxShard + 1) - minShard) {
                // Is this a Splatoon™ reference??
                logger.info { "Booyah! All shards were successfully resumed and no connection needs to be started from scratch!" }
            }

            val upgradedGateways = gatewayResults.map {
                when (it) {
                    is GatewayBootstrapResult.ResumedGateway -> {
                        logger.info { "Changing status of shard ${it.shardId} to resuming..." }
                        it.status.value = DeviousGateway.Status.RESUMING

                        UpgradedGatewayResult.ResumedGateway(
                            DeviousGateway(
                                it.gateway,
                                it.shardId,
                                it.status,
                                it.receivedEventsJob,
                                it.receivedEvents
                            ),
                            it.database,
                            it.cacheEntityMaps,
                            it.gatewaySession
                        )
                    }

                    is GatewayBootstrapResult.FailedToResume -> {
                        logger.info { "Starting fresh shard ${it.shardId}..." }
                        val gateway = DefaultGateway {
                            // The default reconnectRetry is 10, but let's try reconnecting indefinitely (well, kind of, it will try reconnecting MAX_VALUE times)
                            this.reconnectRetry = LinearRetry(2.seconds, 20.seconds, Int.MAX_VALUE)

                            this.identifyRateLimiter = it.parallelIdentifyRateLimiter
                        }

                        val receivedEvents = EventsChannel()
                        val receivedEventsJob = gateway.launch {
                            gateway.events.collect {
                                // Collect events to our channel
                                // This job should NOT be cancelled!
                                receivedEvents.send(it)
                            }
                        }

                        val gatewayJob = gatewayScope.launch {
                            it.status.value = DeviousGateway.Status.WAITING_TO_CONNECT
                            gateway.start(config.loritta.discord.token, createGatewayConfiguration(config.loritta.discord.activity.name, lorittaCluster, it.shardId, config.loritta.discord.maxShards))
                        }

                        UpgradedGatewayResult.FreshGateway(
                            DeviousGateway(
                                gateway,
                                it.shardId,
                                it.status,
                                receivedEventsJob,
                                receivedEvents
                            )
                        )
                    }
                }
            }

            logger.info { "Moving over to LorittaBot instance..." }
            // Iniciar instância da Loritta
            val loritta = LorittaBot(
                clusterId,
                config,
                languageManager,
                localeManager,
                services,
                upgradedGateways,
                identifyLock
            )
            loritta.start()
        }
    }

    private fun copyFromJar(inputPath: String, outputPath: String) {
        val inputStream = LorittaLauncher::class.java.getResourceAsStream(inputPath)
        File(outputPath).writeBytes(inputStream.readAllBytes())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun installCoroutinesDebugProbes() {
        // It is recommended to set this to false to avoid performance hits with the DebugProbes option!
        DebugProbes.enableCreationStackTraces = false
        DebugProbes.install()
    }

    private fun createGatewayConfiguration(activityText: String, lorittaCluster: LorittaConfig.LorittaClustersConfig.LorittaClusterConfig, shardId: Int, totalShards: Int): GatewayConfigurationBuilder.() -> (Unit) = {
        @OptIn(PrivilegedIntent::class)
        intents = Intents {
            +Intent.Guilds
            +Intent.GuildMembers
            +Intent.MessageContent
            +Intent.GuildEmojis
            +Intent.GuildBans
            +Intent.GuildInvites
            +Intent.GuildMessageReactions
            +Intent.GuildVoiceStates
            +Intent.GuildMessages
            +Intent.DirectMessages
            +Intent.DirectMessagesReactions
        }

        presence {
            status = PresenceStatus.Online
            playing(DeviousShard.createActivityTextWithShardAndClusterId(activityText, lorittaCluster, shardId))
        }

        shard = DiscordShard(shardId, totalShards)
    }

    sealed class GatewayBootstrapResult(val shardId: Int) {
        class ResumedGateway(
            shardId: Int,
            val gateway: DefaultGateway,
            val database: Database,
            val cacheEntityMaps: CacheEntityMaps,
            val gatewaySession: DeviousGatewaySession,
            val parallelIdentifyRateLimiter: ParallelIdentifyRateLimiter,
            val status: MutableStateFlow<DeviousGateway.Status>,
            val receivedEventsJob: Job,
            val receivedEvents: EventsChannel
        ) : GatewayBootstrapResult(shardId)

        class FailedToResume(
            shardId: Int,
            val parallelIdentifyRateLimiter: ParallelIdentifyRateLimiter,
            val status: MutableStateFlow<DeviousGateway.Status>,
        ) : GatewayBootstrapResult(shardId)
    }

    sealed class UpgradedGatewayResult {
        class ResumedGateway(
            val deviousGateway: DeviousGateway,
            val cacheDatabase: Database,
            val cacheEntityMaps: CacheEntityMaps,
            val gatewaySession: DeviousGatewaySession
        ) : UpgradedGatewayResult()

        class FreshGateway(
            val deviousGateway: DeviousGateway
        ) : UpgradedGatewayResult()
    }
}
