package net.perfectdreams.loritta.cinnamon.discord.webserver

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import io.ktor.client.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.debug.DebugProbes
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.locale.LorittaLanguageManager
import net.perfectdreams.loritta.cinnamon.utils.config.ConfigUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.InteractionsMetrics
import net.perfectdreams.loritta.cinnamon.discord.webserver.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.utils.HostnameUtils
import java.util.*

object LorittaCinnamonWebServerLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        // https://github.com/JetBrains/Exposed/issues/1356
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        installCoroutinesDebugProbes()

        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(LorittaCinnamonWebServer::class, ConfigUtils.defaultConfigFileName)
        logger.info { "Loaded Loritta's configuration file" }

        val replicaId = if (rootConfig.replicas.getReplicaIdFromHostname) {
            val hostname = HostnameUtils.getHostname()
            hostname.substringAfterLast("-").toIntOrNull() ?: error("Replicas is enabled, but I couldn't get the Replica ID from the hostname!")
        } else {
            rootConfig.replicas.replicaIdOverride ?: 0
        }

        InteractionsMetrics.registerJFRExports()
        InteractionsMetrics.registerInteractions()

        logger.info { "Registered Prometheus Metrics" }

        val languageManager = LorittaLanguageManager(LorittaCinnamonWebServer::class)

        val http = HttpClient {
            expectSuccess = false
        }

        val services = Pudding.createPostgreSQLPudding(
            rootConfig.cinnamon.services.pudding.address,
            rootConfig.cinnamon.services.pudding.database,
            rootConfig.cinnamon.services.pudding.username,
            rootConfig.cinnamon.services.pudding.password
        ) {
            // Because GuildCreates are very I/O intensive, so we are going to increase from the default 30 to 120
            maximumPoolSize = 120
        }
        services.setupShutdownHook()

        logger.info { "Started Pudding client!" }

        val queueConnection = createPostgreSQLConnection(
            rootConfig.queueDatabase.address,
            rootConfig.queueDatabase.database,
            rootConfig.queueDatabase.username,
            rootConfig.queueDatabase.password,
        )

        val loritta = LorittaCinnamonWebServer(
            rootConfig,
            languageManager,
            services,
            queueConnection,
            http,
            replicaId
        )

        loritta.start()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun installCoroutinesDebugProbes() {
        // It is recommended to set this to false, to avoid performance hits with the DebugProbes option!
        DebugProbes.enableCreationStackTraces = false
        DebugProbes.install()
    }

    // This is from Pudding, sightly modified
    private fun createPostgreSQLConnection(
        address: String,
        databaseName: String,
        username: String,
        password: String,
        builder: HikariConfig.() -> (Unit) = {}
    ): HikariDataSource {
        val hikariConfig = createHikariConfig(builder)
        hikariConfig.jdbcUrl = "jdbc:postgresql://$address/$databaseName?ApplicationName=${"Loritta Event Queue - " + HostnameUtils.getHostname()}"

        hikariConfig.username = username
        hikariConfig.password = password

        return HikariDataSource(hikariConfig)
    }

    private fun createHikariConfig(builder: HikariConfig.() -> (Unit)): HikariConfig {
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

        hikariConfig.maximumPoolSize = 1 // // Yes, only one, because only ONE transaction should be used in the ProcessDiscordGatewayEvents class
        hikariConfig.poolName = "QueuePool"
        // Disable synchronous commit to increase throughput
        // Because all of these connections will only be used for gateway event queue, we can set the synchronous_commit on the session itself
        hikariConfig.connectionInitSql = "SET SESSION synchronous_commit = 'off'"

        hikariConfig.apply(builder)

        return hikariConfig
    }
}