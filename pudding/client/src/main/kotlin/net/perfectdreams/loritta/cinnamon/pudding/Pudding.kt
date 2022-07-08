package net.perfectdreams.loritta.cinnamon.pudding

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.perfectdreams.exposedpowerutils.sql.createOrUpdatePostgreSQLEnum
import net.perfectdreams.loritta.cinnamon.common.achievements.AchievementType
import net.perfectdreams.loritta.cinnamon.common.commands.ApplicationCommandType
import net.perfectdreams.loritta.cinnamon.common.components.ComponentType
import net.perfectdreams.loritta.cinnamon.common.utils.DivineInterventionTransactionEntryAction
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaBovespaBrokerUtils
import net.perfectdreams.loritta.cinnamon.common.utils.PendingImportantNotificationState
import net.perfectdreams.loritta.cinnamon.common.utils.SparklyPowerLSXTransactionEntryAction
import net.perfectdreams.loritta.cinnamon.pudding.services.BackgroundsService
import net.perfectdreams.loritta.cinnamon.pudding.services.BetsService
import net.perfectdreams.loritta.cinnamon.pudding.services.BovespaBrokerService
import net.perfectdreams.loritta.cinnamon.pudding.services.ExecutedInteractionsLogService
import net.perfectdreams.loritta.cinnamon.pudding.services.InteractionsDataService
import net.perfectdreams.loritta.cinnamon.pudding.services.MarriagesService
import net.perfectdreams.loritta.cinnamon.pudding.services.NotificationsService
import net.perfectdreams.loritta.cinnamon.pudding.services.PackagesTrackingService
import net.perfectdreams.loritta.cinnamon.pudding.services.PatchNotesNotificationsService
import net.perfectdreams.loritta.cinnamon.pudding.services.PaymentsService
import net.perfectdreams.loritta.cinnamon.pudding.services.ProfileDesignsService
import net.perfectdreams.loritta.cinnamon.pudding.services.ServerConfigsService
import net.perfectdreams.loritta.cinnamon.pudding.services.ShipEffectsService
import net.perfectdreams.loritta.cinnamon.pudding.services.SonhosService
import net.perfectdreams.loritta.cinnamon.pudding.services.StatsService
import net.perfectdreams.loritta.cinnamon.pudding.services.UsersService
import net.perfectdreams.loritta.cinnamon.pudding.tables.BackgroundPayments
import net.perfectdreams.loritta.cinnamon.pudding.tables.BackgroundVariations
import net.perfectdreams.loritta.cinnamon.pudding.tables.Backgrounds
import net.perfectdreams.loritta.cinnamon.pudding.tables.BannedUsers
import net.perfectdreams.loritta.cinnamon.pudding.tables.BoughtStocks
import net.perfectdreams.loritta.cinnamon.pudding.tables.CachedDiscordUsers
import net.perfectdreams.loritta.cinnamon.pudding.tables.CachedDiscordUsersDirectMessageChannels
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetGlobalMatchmakingQueue
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetGlobalMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetGlobalSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.Dailies
import net.perfectdreams.loritta.cinnamon.pudding.tables.DailyTaxUsersToSkipDirectMessages
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightMatches
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightParticipants
import net.perfectdreams.loritta.cinnamon.pudding.tables.ExecutedComponentsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.GuildCountStats
import net.perfectdreams.loritta.cinnamon.pudding.tables.InteractionsData
import net.perfectdreams.loritta.cinnamon.pudding.tables.Marriages
import net.perfectdreams.loritta.cinnamon.pudding.tables.PatchNotesNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.PaymentSonhosTransactionResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.Payments
import net.perfectdreams.loritta.cinnamon.pudding.tables.PendingImportantNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.ProfileDesignGroups
import net.perfectdreams.loritta.cinnamon.pudding.tables.ProfileDesigns
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.ReceivedPatchNotesNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.Sets
import net.perfectdreams.loritta.cinnamon.pudding.tables.ShipEffects
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosBundles
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.TickerPrices
import net.perfectdreams.loritta.cinnamon.pudding.tables.TrackedCorreiosPackages
import net.perfectdreams.loritta.cinnamon.pudding.tables.TrackedCorreiosPackagesEvents
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserAchievements
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserSettings
import net.perfectdreams.loritta.cinnamon.pudding.tables.UsersFollowingCorreiosPackages
import net.perfectdreams.loritta.cinnamon.pudding.tables.bomdiaecia.BomDiaECiaMatches
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.DiscordGuildMembers
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.DiscordGuilds
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.CorreiosPackageUpdateUserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.DailyTaxTaxedUserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.DailyTaxWarnUserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.UserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.BrokerSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.CoinFlipBetSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.DailyTaxSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.DivineInterventionSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.EmojiFightSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.ExecutedApplicationCommandsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.PaymentSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.ShipEffectSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.SonhosBundlePurchaseSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.SparklyPowerLSXSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.utils.PuddingTasks
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.DEFAULT_REPETITION_ATTEMPTS
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class Pudding(val hikariDataSource: HikariDataSource, val database: Database) {
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
        fun createPostgreSQLPudding(
            address: String,
            databaseName: String,
            username: String,
            password: String
        ): Pudding {
            val hikariConfig = createHikariConfig()
            hikariConfig.jdbcUrl = "jdbc:postgresql://$address/$databaseName?ApplicationName=${getPuddingApplicationName()}"

            hikariConfig.username = username
            hikariConfig.password = password

            val hikariDataSource = HikariDataSource(hikariConfig)

            return Pudding(hikariDataSource, connectToDatabase(hikariDataSource))
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

            // https://stackoverflow.com/a/64375936/7271796
            hikariConfig.isAllowPoolSuspension = true

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

        private fun getPuddingApplicationName(): String {
            val suffix = "Loritta Cinnamon Pudding"
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

            DiscordGuilds,
            DiscordGuildMembers
        )

        if (schemas.isNotEmpty())
            transaction {
                createOrUpdatePostgreSQLEnum(AchievementType.values())
                createOrUpdatePostgreSQLEnum(ApplicationCommandType.values())
                createOrUpdatePostgreSQLEnum(ComponentType.values())
                createOrUpdatePostgreSQLEnum(LorittaBovespaBrokerUtils.BrokerSonhosTransactionsEntryAction.values())
                createOrUpdatePostgreSQLEnum(SparklyPowerLSXTransactionEntryAction.values())
                createOrUpdatePostgreSQLEnum(DivineInterventionTransactionEntryAction.values())
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
    suspend fun <T> transaction(repetitions: Int = 5, transactionIsolation: Int? = null, statement: suspend org.jetbrains.exposed.sql.Transaction.() -> T): T {
        var lastException: Exception? = null
        for (i in 1..repetitions) {
            try {
                return org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction(Dispatchers.IO, database, transactionIsolation) {
                    statement.invoke(this)
                }
            } catch (e: ExposedSQLException) {
                logger.warn(e) { "Exception while trying to execute query. Tries: $i" }
                lastException = e
            }
        }
        throw lastException ?: RuntimeException("This should never happen")
    }

    private val mutex = Mutex()

    /**
     * Runs an IO bound code within a transaction (Example: Interactions with Discord's API)
     *
     * This is useful to avoid all transactions being exausted due to Discord's rate limit.
     *
     * To handle it, HikariCP's maximumPoolSize is increased and then decreased after the code finishes its execution.
     */
    suspend fun <T> runIOBound(block: suspend () -> (T)): T {
        if (TransactionManager.currentOrNull() == null)
            error("This can only be ran within a transaction block!")

        val config = hikariDataSource.hikariConfigMXBean

        mutex.withLock {
            if (config.minimumIdle == 0) {
                logger.info { "Running IO Bound code for the first time, setting minimum idle to ${config.maximumPoolSize}" }
                config.minimumIdle = config.maximumPoolSize
            }

            config.maximumPoolSize = config.maximumPoolSize + 1
            logger.info { "Running IO Bound code, increased pool size to ${config.maximumPoolSize}" }
        }

        try {
            return block.invoke()
        } finally {
            mutex.withLock {
                config.maximumPoolSize = config.maximumPoolSize - 1
                logger.info { "Finished running IO Bound code, decreased pool size to ${config.maximumPoolSize}" }
            }
        }
    }

    fun shutdown() {
        puddingTasks.shutdown()
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