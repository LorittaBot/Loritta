package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import jdk.internal.misc.Signal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.debug.DebugProbes
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.common.utils.config.ConfigUtils
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.QueueDatabase
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import org.jetbrains.exposed.sql.DEFAULT_REPETITION_ATTEMPTS
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

object DiscordGatewayEventsProcessorLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        // https://github.com/JetBrains/Exposed/issues/1356
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(
            DiscordGatewayEventsProcessorLauncher::class, System.getProperty("discordgatewayeventsprocessor.config", "discord-gateway-events-processor.conf"))
        logger.info { "Loaded Loritta's configuration file" }

        val languageManager = LanguageManager(
            DiscordGatewayEventsProcessor::class,
            "en",
            "/languages/"
        )
        languageManager.loadLanguagesAndContexts()

        val services = Pudding.createPostgreSQLPudding(
            rootConfig.pudding.address,
            rootConfig.pudding.database,
            rootConfig.pudding.username,
            rootConfig.pudding.password
        )

        Runtime.getRuntime().addShutdownHook(
            thread(false) {
                // Shutdown services when stopping the application
                // This is needed for the Pudding Tasks
                services.shutdown()
            }
        )

        logger.info { "Started Pudding client!" }

        val queueDatabase = createPostgreSQLDatabaseConnection(
            rootConfig.queueDatabase.address,
            rootConfig.queueDatabase.database,
            rootConfig.queueDatabase.username,
            rootConfig.queueDatabase.password
        )

        val loritta = DiscordGatewayEventsProcessor(
            rootConfig,
            services,
            queueDatabase,
            languageManager
        )

        loritta.start()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun installCoroutinesDebugProbes() {
        // It is recommended to set this to false to avoid performance hits with the DebugProbes option!
        DebugProbes.enableCreationStackTraces = false
        DebugProbes.install()

        Signal.handle(Signal("TRAP")) { signal ->
            DebugProbes.dumpCoroutines()
        }
    }

    fun createPostgreSQLDatabaseConnection(address: String, databaseName: String, username: String, password: String): QueueDatabase {
        val hikariConfig = createHikariConfig()
        hikariConfig.jdbcUrl = "jdbc:postgresql://$address/$databaseName?ApplicationName=${getPuddingApplicationName()}"

        hikariConfig.username = username
        hikariConfig.password = password

        val hikariDataSource = HikariDataSource(hikariConfig)
        hikariDataSource.maximumPoolSize = 1 // Yes, only one, because only ONE transaction should be used in the ProcessDiscordGatewayEvents class

        return QueueDatabase(
            Database.connect(
                hikariDataSource,
                databaseConfig = DatabaseConfig {
                    defaultRepetitionAttempts = DEFAULT_REPETITION_ATTEMPTS
                    defaultIsolationLevel = IsolationLevel.TRANSACTION_READ_COMMITTED.levelId // Change our default isolation level
                }
            ),
            hikariDataSource
        )
    }

    private fun createHikariConfig(): HikariConfig {
        val hikariConfig = HikariConfig()

        hikariConfig.driverClassName = "org.postgresql.Driver"

        // https://github.com/JetBrains/Exposed/wiki/DSL#batch-insert
        hikariConfig.addDataSourceProperty("reWriteBatchedInserts", "true")

        // Exposed uses autoCommit = false, so we need to set this to false to avoid HikariCP resetting the connection to
        // autoCommit = true when the transaction goes back to the pool, because resetting this has a "big performance impact"
        // https://stackoverflow.com/a/41206003/7271796
        hikariConfig.isAutoCommit = false

        // Useful to check if a connection is not returning to the pool, will be shown in the log as "Apparent connection leak detected"
        hikariConfig.leakDetectionThreshold = 30L * 1000
        hikariConfig.transactionIsolation = IsolationLevel.TRANSACTION_READ_COMMITTED.name

        return hikariConfig
    }

    private fun getPuddingApplicationName(): String {
        val suffix = "Loritta Discord Gateway Events Processor"
        // From hostname command
        try {
            val proc = ProcessBuilder("hostname")
                .start()

            proc.waitFor(5, TimeUnit.SECONDS)
            val hostname = proc.inputStream.readAllBytes().toString(Charsets.UTF_8).removeSuffix("\n")
            proc.destroyForcibly()

            logger.warn { "Machine Hostname via \"hostname\" command: $hostname" }
            return "$suffix - $hostname"
        } catch (e: Exception) {
            logger.warn(e) { "Something went wrong while trying to get the machine's hostname via the \"hostname\" command!" }
        }

        // From hostname env variable
        System.getenv("HOSTNAME")?.let {
            logger.warn { "Machine Hostname via \"HOSTNAME\" env variable: $it" }
            return "$suffix - $it"
        }

        // From computername env variable
        System.getenv("COMPUTERNAME")?.let {
            logger.warn { "Machine Hostname via \"COMPUTERNAME\" env variable: $it" }
            return "$suffix - $it"
        }

        logger.warn { "I wasn't able to get the machine's hostname! Falling back to \"Unknown\"..." }
        return "$suffix - Unknown"
    }
}