package net.perfectdreams.loritta.cinnamon.pudding

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.metrics.prometheus.PrometheusMetricsTrackerFactory
import com.zaxxer.hikari.util.IsolationLevel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import mu.KotlinLogging
import net.perfectdreams.exposedpowerutils.sql.createOrUpdatePostgreSQLEnum
import net.perfectdreams.loritta.cinnamon.common.achievements.AchievementType
import net.perfectdreams.loritta.cinnamon.common.commands.ApplicationCommandType
import net.perfectdreams.loritta.cinnamon.common.components.ComponentType
import net.perfectdreams.loritta.cinnamon.common.utils.*
import net.perfectdreams.loritta.cinnamon.pudding.services.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.bomdiaecia.BomDiaECiaMatches
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.CorreiosPackageUpdateUserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.DailyTaxTaxedUserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.DailyTaxWarnUserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.UserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.*
import net.perfectdreams.loritta.cinnamon.pudding.utils.PuddingTasks
import net.perfectdreams.loritta.cinnamon.pudding.utils.metrics.PuddingMetrics
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.security.SecureRandom
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class Pudding(
    val hikariDataSource: HikariDataSource,
    val database: Database,
    private val cachedThreadPool: ExecutorService,
    val dispatcher: CoroutineDispatcher
) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val DRIVER_CLASS_NAME = "org.postgresql.Driver"
        private val ISOLATION_LEVEL =
            IsolationLevel.TRANSACTION_REPEATABLE_READ // We use repeatable read to avoid dirty and non-repeatable reads! Very useful and safe!!

        /**
         * Creates a Pudding instance backed by a PostgreSQL database
         *
         * @param address      the PostgreSQL address
         * @param databaseName the database name in PostgreSQL
         * @param username     the PostgreSQL username
         * @param password     the PostgreSQL password
         * @return a [Pudding] instance backed by a PostgreSQL database
         */
        @ExperimentalCoroutinesApi
        fun createPostgreSQLPudding(
            address: String,
            databaseName: String,
            username: String,
            password: String
        ): Pudding {
            val hikariConfig = createHikariConfig()
            hikariConfig.jdbcUrl = "jdbc:postgresql://$address/$databaseName?ApplicationName=${"Loritta Cinnamon Pudding - " + HostnameUtils.getHostname()}"

            hikariConfig.username = username
            hikariConfig.password = password

            val hikariDataSource = HikariDataSource(hikariConfig)

            val cachedThreadPool = Executors.newCachedThreadPool()

            return Pudding(
                hikariDataSource,
                connectToDatabase(hikariDataSource),
                cachedThreadPool,
                // Instead of using Dispatchers.IO directly, we will create a cached thread pool.
                // This avoids issues when all Dispatchers.IO threads are blocked on transactions, causing any other coroutine using the Dispatcher.IO job to be
                // blocked.
                // Example: 64 blocked coroutines due to transactions (64 = max threads in a Dispatchers.IO dispatcher) + you also have a WebSocket listening for events, when the WS tries to
                // read incoming events, it is blocked because there isn't any available Dispatchers.IO threads!
                cachedThreadPool.asCoroutineDispatcher()
            )
        }

        private fun createHikariConfig(): HikariConfig {
            val hikariConfig = HikariConfig()

            hikariConfig.driverClassName = DRIVER_CLASS_NAME

            // https://github.com/JetBrains/Exposed/wiki/DSL#batch-insert
            hikariConfig.addDataSourceProperty("reWriteBatchedInserts", "true")

            // Exposed uses autoCommit = false, so we need to set this to false to avoid HikariCP resetting the connection to
            // autoCommit = true when the transaction goes back to the pool, because resetting this has a "big performance impact"
            // https://stackoverflow.com/a/41206003/7271796
            hikariConfig.isAutoCommit = false

            // Useful to check if a connection is not returning to the pool, will be shown in the log as "Apparent connection leak detected"
            hikariConfig.leakDetectionThreshold = 30L * 1000
            hikariConfig.transactionIsolation = ISOLATION_LEVEL.name // We use repeatable read to avoid dirty and non-repeatable reads! Very useful and safe!!
            hikariConfig.metricsTrackerFactory = PrometheusMetricsTrackerFactory()

            // Our dedicated db server has 16 cores, so we (16 * 2) like what's described in https://wiki.postgresql.org/wiki/Number_Of_Database_Connections
            hikariConfig.maximumPoolSize = 30

            return hikariConfig
        }

        // Loritta (Legacy) uses this!
        fun connectToDatabase(dataSource: HikariDataSource): Database =
            Database.connect(
                dataSource,
                databaseConfig = DatabaseConfig {
                    defaultRepetitionAttempts = DEFAULT_REPETITION_ATTEMPTS
                    defaultIsolationLevel = ISOLATION_LEVEL.levelId // Change our default isolation level
                }
            )
    }

    val users = UsersService(this)
    val serverConfigs = ServerConfigsService(this)
    val sonhos = SonhosService(this)
    val shipEffects = ShipEffectsService(this)
    val marriages = MarriagesService(this)
    val interactionsData = InteractionsDataService(this)
    val executedInteractionsLog = ExecutedInteractionsLogService(this)
    val backgrounds = BackgroundsService(this)
    val profileDesigns = ProfileDesignsService(this)
    val bovespaBroker = BovespaBrokerService(this)
    val bets = BetsService(this)
    val payments = PaymentsService(this)
    val stats = StatsService(this)
    val puddingTasks = PuddingTasks(this)
    val patchNotesNotifications = PatchNotesNotificationsService(this)
    val packagesTracking = PackagesTrackingService(this)
    val notifications = NotificationsService(this)

    // Used to avoid having a lot of threads being created on the "dispatcher" just to be blocked waiting for a connection, causing thread starvation and an OOM kill
    private val semaphore = Semaphore(128)
    val random = SecureRandom()

    /**
     * Starts tasks related to [Pudding], like table partition creation, purge old data, etc.
     *
     * If you are using Pudding just to interact with tables, and you don't care about the tasks, then you don't need to start the tasks!
     */
    fun startPuddingTasks() = puddingTasks.start()

    /**
     * Creates missing tables and columns in the database.
     *
     * To avoid updating tables that are being used by legacy applications (Loritta Legacy), the [shouldBeUpdated] parameter
     * can be used to filter what tables should be created.
     *
     * @param shouldBeUpdated checks if a table should be created/updated or not, if true, it will be included to be updated, if false, it won't
     */
    suspend fun createMissingTablesAndColumns(shouldBeUpdated: (String) -> Boolean) {
        val schemas = mutableListOf<Table>()
        fun insertIfValid(vararg tables: Table) =
            schemas.addAll(tables.filter { shouldBeUpdated.invoke(it::class.simpleName!!) })
        insertIfValid(
            Sets,
            ProfileDesignGroups,
            ProfileDesigns,
            Backgrounds,
            BackgroundVariations,
            BackgroundPayments,
            UserSettings,
            Profiles,
            ServerConfigs,
            ShipEffects,
            Marriages,
            UserAchievements,
            InteractionsData,
            ExecutedApplicationCommandsLog,
            ExecutedComponentsLog,
            TickerPrices,
            BoughtStocks,
            BannedUsers,
            SonhosTransactionsLog,
            BrokerSonhosTransactionsLog,
            CachedDiscordUsers,
            CoinFlipBetGlobalMatchmakingQueue,
            CoinFlipBetGlobalMatchmakingResults,
            CoinFlipBetGlobalSonhosTransactionsLog,
            SparklyPowerLSXSonhosTransactionsLog,
            Dailies,
            Payments,
            GuildCountStats,
            CachedDiscordUsersDirectMessageChannels,
            DailyTaxSonhosTransactionsLog,
            DailyTaxUsersToSkipDirectMessages,
            CoinFlipBetMatchmakingResults,
            CoinFlipBetSonhosTransactionsLog,
            EmojiFightMatches,
            EmojiFightParticipants,
            EmojiFightMatchmakingResults,
            EmojiFightSonhosTransactionsLog,
            DivineInterventionSonhosTransactionsLog,
            BotVoteSonhosTransactionsLog,
            PaymentSonhosTransactionResults,
            PaymentSonhosTransactionsLog,
            SonhosBundles,
            SonhosBundlePurchaseSonhosTransactionsLog,
            PatchNotesNotifications,
            ReceivedPatchNotesNotifications,
            TrackedCorreiosPackages,
            UsersFollowingCorreiosPackages,
            TrackedCorreiosPackagesEvents,
            PendingImportantNotifications,
            ShipEffectSonhosTransactionsLog,
            UserNotifications,
            DailyTaxTaxedUserNotifications,
            DailyTaxWarnUserNotifications,
            CorreiosPackageUpdateUserNotifications,
            BomDiaECiaMatches,
            BotVoteSonhosTransactionsLog,

            DiscordGuilds,
            DiscordGuildMembers,
            DiscordRoles,
            DiscordChannels,
            DiscordEmojis
        )

        if (schemas.isNotEmpty())
            transaction {
                createOrUpdatePostgreSQLEnum(AchievementType.values())
                createOrUpdatePostgreSQLEnum(ApplicationCommandType.values())
                createOrUpdatePostgreSQLEnum(ComponentType.values())
                createOrUpdatePostgreSQLEnum(LorittaBovespaBrokerUtils.BrokerSonhosTransactionsEntryAction.values())
                createOrUpdatePostgreSQLEnum(SparklyPowerLSXTransactionEntryAction.values())
                createOrUpdatePostgreSQLEnum(DivineInterventionTransactionEntryAction.values())
                createOrUpdatePostgreSQLEnum(WebsiteVoteSource.values())
                createOrUpdatePostgreSQLEnum(PendingImportantNotificationState.values())

                logger.info { "Tables to be created or updated: $schemas" }
                SchemaUtils.createMissingTablesAndColumns(
                    *schemas
                        .toMutableList()
                        // Partitioned tables
                        .filter { it !in listOf(ExecutedApplicationCommandsLog, ExecutedComponentsLog) }
                        .toTypedArray()
                )

                // This is a workaround because Exposed does not support (yet) Partitioned Tables
                if (ExecutedApplicationCommandsLog in schemas)
                    createPartitionedTable(ExecutedApplicationCommandsLog)
                if (ExecutedComponentsLog in schemas)
                    createPartitionedTable(ExecutedComponentsLog)
            }
    }

    private fun Transaction.createPartitionedTable(table: Table) {
        // The reason we use partitioned tables for the ExecutedApplicationCommandsLog table, is because there is a LOT of commands there
        // Removing old logs is painfully slow due to vacuuming and stuff, querying recent commands is also pretty slow.
        // So it is better to split stuff up in separate partitions!
        val createStatements = createStatementsPartitioned(table, "RANGE(sent_at)")

        execStatements(false, createStatements)
        commit()

        val alterStatements = SchemaUtils.addMissingColumnsStatements(
            table
        )

        // Now call the addMissingColumnsStatements again with the partitioned tables
        // We can not use createMissingTablesAndColumns here because Exposed will think that the table does not exist
        // because it is a partitioned table!
        execStatements(false, alterStatements)
        commit()
    }

    // From Exposed
    private fun Transaction.execStatements(inBatch: Boolean, statements: List<String>) {
        if (inBatch) {
            execInBatch(statements)
        } else {
            for (statement in statements) {
                exec(statement)
            }
        }
    }

    // This is a workaround because "Table.exists()" does not work for partitioned tables!
    private fun Transaction.checkIfTableExists(table: Table): Boolean {
        val tableScheme = table.tableName.substringBefore('.', "").takeIf { it.isNotEmpty() }
        val schema = tableScheme?.inProperCase() ?: TransactionManager.current().connection.metadata { currentScheme }
        val tableName = TransactionManager.current().identity(table) // Yes, because "Table.tableName" does not return the correct name...

        return exec("SELECT EXISTS (\n" +
                "   SELECT FROM information_schema.tables \n" +
                "   WHERE  table_schema = '$schema'\n" +
                "   AND    table_name   = '$tableName'\n" +
                "   )") {
            it.next()
            it.getBoolean(1) // It should always be the first column, right?
        } ?: false
    }

    // From Exposed
    private fun String.inProperCase(): String =
        TransactionManager.currentOrNull()?.db?.identifierManager?.inProperCase(this@inProperCase) ?: this


    // From Exposed, this is the "createStatements" method but with a few changes
    private fun Transaction.createStatementsPartitioned(table: Table, partitionBySuffix: String): List<String> {
        if (checkIfTableExists(table))
            return emptyList()

        val alters = arrayListOf<String>()

        val (create, alter) = table.ddl.partition { it.startsWith("CREATE ") }

        val createTableSuffixed = create.map { "$it PARTITION BY $partitionBySuffix" }

        val indicesDDL = table.indices.flatMap { SchemaUtils.createIndex(it) }
        alters += alter

        return createTableSuffixed + indicesDDL + alter
    }

    // https://github.com/JetBrains/Exposed/issues/1003
    suspend fun <T> transaction(repetitions: Int = 5, transactionIsolation: Int? = null, statement: Transaction.() -> T): T {
        var lastException: Exception? = null
        for (i in 1..repetitions) {
            try {
                PuddingMetrics.availablePermits.set(semaphore.availablePermits.toDouble())
                semaphore.withPermit {
                    return org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction(
                        dispatcher,
                        database,
                        transactionIsolation
                    ) {
                        statement.invoke(this)
                    }
                }
            } catch (e: ExposedSQLException) {
                logger.warn(e) { "Exception while trying to execute query. Tries: $i" }
                lastException = e
            }
        }
        throw lastException ?: RuntimeException("This should never happen")
    }

    fun shutdown() {
        puddingTasks.shutdown()
        cachedThreadPool.shutdown()
    }

    /**
     * Setups a shutdown hook to shut down the [puddingTasks] when the application shutdowns.
     */
    fun setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(
            thread(false) {
                // Shutdown services when stopping the application
                // This is needed for the Pudding Tasks
                shutdown()
            }
        )
    }
}
