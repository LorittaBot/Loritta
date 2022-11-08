package net.perfectdreams.loritta.morenitta

import it.unimi.dsi.fastutil.longs.Long2LongMaps
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.debug.DebugProbes
import kotlinx.coroutines.runBlocking
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
import net.perfectdreams.loritta.deviousfun.tables.*
import net.perfectdreams.loritta.deviousfun.utils.CacheEntityMaps
import net.perfectdreams.loritta.deviousfun.utils.DeviousGuildDataWrapper
import net.perfectdreams.loritta.deviousfun.utils.SnowflakeMap
import net.perfectdreams.loritta.morenitta.utils.config.BaseConfig
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

/**
 * Loritta's Launcher
 *
 * @author MrPowerGamerBR
 */
object LorittaLauncher {
    private val logger = KotlinLogging.logger {}
    private val OFFSET_SIZE = 512

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

        // Cache Database
        // Improve throughput
        // https://phiresky.github.io/blog/2020/sqlite-performance-tuning/
        // journal_mode = WAL improves performance
        // synchronous = We don't care about data loss
        // temp_store = We don't want to store indexes in memory, we already have the data stored in memory
        // locking_mode = reduces the number of syscalls required, only one process (the Loritta instance) will access the database
        val database = Database.connect("jdbc:sqlite:cache/lori_devious.db?journal_mode=wal&synchronous=off&mmap_size=30000000000&temp_store=0&page_size=32768&locking_mode=exclusive")

        val cacheEntityMaps = runBlocking {
            val (cacheEntityMaps, duration) = measureTimedValue {
                transaction(Dispatchers.IO, database) {
                    SchemaUtils.createMissingTablesAndColumns(
                        Users,
                        Guilds,
                        GuildChannels,
                        GuildMembers,
                        GuildRoles,
                        GuildEmojis,
                        GuildVoiceStates,
                        GatewaySessions
                    )

                    logger.info { "Loading users..." }
                    val userCount = Users.selectAll().count()
                    logger.info { "User Count: $userCount" }
                    val users = SnowflakeMap<DeviousUserData>(userCount.toInt())

                    Users.selectAll()
                        .forEach {
                            users[LightweightSnowflake(it[Users.id])] = Json.decodeFromString(it[Users.data])
                        }

                    logger.info { "Loading guilds..." }
                    val guildCount = Guilds.selectAll().count()
                    logger.info { "Guild Count: $guildCount" }
                    val guilds = SnowflakeMap<DeviousGuildDataWrapper>(guildCount.toInt())

                    Guilds.selectAll()
                        .forEach {
                            guilds[LightweightSnowflake(it[Guilds.id])] = Json.decodeFromString(it[Guilds.data])
                        }

                    logger.info { "Loading members..." }
                    val mutableMaps = SnowflakeMap<SnowflakeMap<DeviousMemberData>>(guildCount.toInt())
                    GuildMembers.selectAll()
                        .forEach {
                            val guildMap = mutableMaps.getOrPut(LightweightSnowflake(it[GuildMembers.guildId])) { SnowflakeMap(0) }
                            guildMap[LightweightSnowflake(it[GuildMembers.userId])] = Json.decodeFromString(it[GuildMembers.data])
                        }

                    val members = SnowflakeMap<SnowflakeMap<DeviousMemberData>>(mutableMaps.size)
                    for (map in mutableMaps.backedMap) {
                        members.backedMap[map.key] = map.value
                    }

                    logger.info { "Loading channels..." }
                    val channelCount = GuildChannels.selectAll().count()
                    logger.info { "Channel Count: $channelCount" }
                    val guildChannels = SnowflakeMap<SnowflakeMap<DeviousChannelData>>(channelCount.toInt())
                    // The expected value for this map isn't *really* correct, but it doesn't need to be correct
                    // In *theory*, the amount of channels is around ~35x the size of guilds, at least according to calculations
                    // Loaded Guilds: 116736
                    // Loaded Channels: 3864943
                    val channelsToGuilds = Long2LongMaps.synchronize(Long2LongOpenHashMap((channelCount * 35).toInt()))

                    // Loading all the data without a limit causes a lot of GC pressure, so we will load the data in steps to reduce the pressure
                    // (Exposed has a "selectAllBatched" but it doesn't work because it requires a auto incrementing column)
                    //
                    // Using jdbc's getBinaryStream/getAsciiStream doesn't seem to give a performance boost, in fact, it actually takes *more* time to deserialize with kotlinx.serialization!
                    fun Table.selectAllForEachWithOffset(tableSize: Int, offsetSize: Int, action: (ResultRow) -> (Unit)) {
                        for (offset in 0 until tableSize step offsetSize) {
                            this.selectAll()
                                .fetchSize(offsetSize) // Doesn't really help since we are using SQLite
                                .limit(offsetSize, offset.toLong())
                                .forEach(action)
                            logger.info { "Progress: ${(offset / tableSize.toDouble()) * 100}%" }
                        }
                    }

                    GuildChannels.selectAllForEachWithOffset(channelCount.toInt(), OFFSET_SIZE) {
                        val guildId = LightweightSnowflake(it[GuildChannels.id])

                        val guildChannelsData = Json.decodeFromString<List<DeviousChannelData>>(it[GuildChannels.data])

                        val channelsOfThisGuild = SnowflakeMap<DeviousChannelData>(guildChannelsData.size)

                        for (guildChannel in guildChannelsData) {
                            channelsOfThisGuild[guildChannel.id] = guildChannel
                            channelsToGuilds[guildChannel.id.value.toLong()] = guildId.value.toLong()
                        }

                        guildChannels[LightweightSnowflake(it[GuildChannels.id])] = channelsOfThisGuild
                    }
                    println("Channels: ${guildChannels.values.sumOf { it.size }}")

                    logger.info { "Loading roles..." }
                    val rolesCount = GuildRoles.selectAll().count()
                    logger.info { "Roles Count: $rolesCount" }
                    val roles = SnowflakeMap<SnowflakeMap<DeviousRoleData>>(rolesCount.toInt())
                    GuildRoles.selectAllForEachWithOffset(rolesCount.toInt(), OFFSET_SIZE) {
                        val guildRolesData = Json.decodeFromString<List<DeviousRoleData>>(it[GuildRoles.data])
                        val guildRolesMap = SnowflakeMap<DeviousRoleData>(guildRolesData.size)
                        for (guildRoleData in guildRolesData) {
                            guildRolesMap[guildRoleData.id] = guildRoleData
                        }
                        roles[LightweightSnowflake(it[GuildRoles.id])] = guildRolesMap
                    }

                    logger.info { "Loading emojis..." }
                    val emojisCount = GuildEmojis.selectAll().count()
                    logger.info { "Emojis Count: $rolesCount" }
                    val emotes = SnowflakeMap<SnowflakeMap<DeviousGuildEmojiData>>(emojisCount.toInt())
                    GuildEmojis.selectAllForEachWithOffset(emojisCount.toInt(), OFFSET_SIZE) {
                        val guildEmojisData = Json.decodeFromString<List<DeviousGuildEmojiData>>(it[GuildEmojis.data])
                        val guildEmojisMap = SnowflakeMap<DeviousGuildEmojiData>(guildEmojisData.size)
                        for (guildEmojiData in guildEmojisData) {
                            guildEmojisMap[guildEmojiData.id] = guildEmojiData
                        }
                        emotes[LightweightSnowflake(it[GuildEmojis.id])] = guildEmojisMap
                    }

                    logger.info { "Loading voice states..." }
                    val voiceStatesCount = GuildVoiceStates.selectAll().count()
                    logger.info { "Voice States Count: $voiceStatesCount" }
                    val voiceStates = SnowflakeMap<SnowflakeMap<DeviousVoiceStateData>>(voiceStatesCount.toInt())
                    GuildVoiceStates.selectAllForEachWithOffset(voiceStatesCount.toInt(), OFFSET_SIZE) {
                        val guildVoiceStatesData = Json.decodeFromString<List<DeviousVoiceStateData>>(it[GuildVoiceStates.data])
                        val guildVoiceStatesMap = SnowflakeMap<DeviousVoiceStateData>(guildVoiceStatesData.size)
                        for (guildVoiceState in guildVoiceStatesData) {
                            guildVoiceStatesMap[guildVoiceState.userId] = guildVoiceState
                        }
                        voiceStates[LightweightSnowflake(it[GuildVoiceStates.id])] = guildVoiceStatesMap
                    }

                    logger.info { "Loading gateway sessions..." }
                    val gatewaySessions = ConcurrentHashMap<Int, DeviousGatewaySession>()
                    GatewaySessions.selectAll()
                        .forEach {
                            gatewaySessions[it[GatewaySessions.id]] = DeviousGatewaySession(
                                it[GatewaySessions.sessionId],
                                it[GatewaySessions.resumeGatewayUrl],
                                it[GatewaySessions.sequence],
                            )
                        }

                    CacheEntityMaps(
                        users,
                        guilds,
                        guildChannels,
                        channelsToGuilds,
                        emotes,
                        roles,
                        members,
                        voiceStates,
                        gatewaySessions
                    )
                }
            }

            logger.info { "Data successfully loaded! Took $duration" }

            cacheEntityMaps
        }

        // Iniciar instância da Loritta
        val loritta = LorittaBot(
            clusterId,
            config,
            languageManager,
            localeManager,
            services,
            database,
            cacheEntityMaps
        )
        loritta.start()
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
}
