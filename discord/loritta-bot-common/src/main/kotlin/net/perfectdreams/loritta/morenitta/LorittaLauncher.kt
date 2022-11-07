package net.perfectdreams.loritta.morenitta

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.debug.DebugProbes
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
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
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

/**
 * Loritta's Launcher
 *
 * @author MrPowerGamerBR
 */
object LorittaLauncher {
    private val logger = KotlinLogging.logger {}

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
                        Channels,
                        Guilds,
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
                            val guildMap =
                                mutableMaps.getOrPut(LightweightSnowflake(it[GuildMembers.guildId])) { SnowflakeMap(0) }
                            guildMap[LightweightSnowflake(it[GuildMembers.userId])] =
                                Json.decodeFromString(it[GuildMembers.data])
                        }

                    val members = SnowflakeMap<SnowflakeMap<DeviousMemberData>>(mutableMaps.size)
                    for (map in mutableMaps.backedMap) {
                        members.backedMap[map.key] = map.value
                    }

                    logger.info { "Loading channels..." }
                    val channelCount = Channels.selectAll().count()
                    logger.info { "Channel Count: $channelCount" }
                    val channels = SnowflakeMap<DeviousChannelData>(channelCount.toInt())
                    Channels.selectAll()
                        .forEach {
                            channels[LightweightSnowflake(it[Channels.id])] = Json.decodeFromString(it[Channels.data])
                        }

                    logger.info { "Loading roles..." }
                    val rolesCount = GuildRoles.selectAll().count()
                    logger.info { "Roles Count: $rolesCount" }
                    val roles = SnowflakeMap<SnowflakeMap<DeviousRoleData>>(rolesCount.toInt())
                    GuildRoles.selectAll()
                        .forEach {
                            roles[LightweightSnowflake(it[GuildRoles.id])] =
                                SnowflakeMap(Json.decodeFromString<Map<LightweightSnowflake, DeviousRoleData>>(it[GuildRoles.data]))
                        }

                    logger.info { "Loading emojis..." }
                    val emojisCount = GuildEmojis.selectAll().count()
                    logger.info { "Emojis Count: $rolesCount" }
                    val emotes = SnowflakeMap<SnowflakeMap<DeviousGuildEmojiData>>(emojisCount.toInt())
                    GuildEmojis.selectAll()
                        .forEach {
                            emotes[LightweightSnowflake(it[GuildEmojis.id])] =
                                SnowflakeMap(Json.decodeFromString<Map<LightweightSnowflake, DeviousGuildEmojiData>>(it[GuildEmojis.data]))
                        }

                    logger.info { "Loading voice states..." }
                    val voiceStatesCount = GuildEmojis.selectAll().count()
                    logger.info { "Voice States Count: $voiceStatesCount" }
                    val voiceStates = SnowflakeMap<SnowflakeMap<DeviousVoiceStateData>>(voiceStatesCount.toInt())
                    GuildVoiceStates.selectAll()
                        .forEach {
                            voiceStates[LightweightSnowflake(it[GuildVoiceStates.id])] =
                                SnowflakeMap(Json.decodeFromString<Map<LightweightSnowflake, DeviousVoiceStateData>>(it[GuildVoiceStates.data]))
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
                        channels,
                        guilds,
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
