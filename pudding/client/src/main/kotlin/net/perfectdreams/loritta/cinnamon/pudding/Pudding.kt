package net.perfectdreams.loritta.cinnamon.pudding

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import kotlinx.coroutines.Dispatchers
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.achievements.AchievementType
import net.perfectdreams.loritta.cinnamon.common.commands.ApplicationCommandType
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaBovespaBrokerUtils
import net.perfectdreams.loritta.cinnamon.pudding.services.BackgroundsService
import net.perfectdreams.loritta.cinnamon.pudding.services.BetsService
import net.perfectdreams.loritta.cinnamon.pudding.services.BovespaBrokerService
import net.perfectdreams.loritta.cinnamon.pudding.services.ExecutedApplicationCommandsLogService
import net.perfectdreams.loritta.cinnamon.pudding.services.InteractionsDataService
import net.perfectdreams.loritta.cinnamon.pudding.services.MarriagesService
import net.perfectdreams.loritta.cinnamon.pudding.services.ProfileDesignsService
import net.perfectdreams.loritta.cinnamon.pudding.services.ServerConfigsService
import net.perfectdreams.loritta.cinnamon.pudding.services.ShipEffectsService
import net.perfectdreams.loritta.cinnamon.pudding.services.SonhosService
import net.perfectdreams.loritta.cinnamon.pudding.services.UsersService
import net.perfectdreams.loritta.cinnamon.pudding.tables.BackgroundPayments
import net.perfectdreams.loritta.cinnamon.pudding.tables.BackgroundVariations
import net.perfectdreams.loritta.cinnamon.pudding.tables.Backgrounds
import net.perfectdreams.loritta.cinnamon.pudding.tables.BannedUsers
import net.perfectdreams.loritta.cinnamon.pudding.tables.BoughtStocks
import net.perfectdreams.loritta.cinnamon.pudding.tables.BrokerSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.CachedDiscordUsers
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinflipGlobalMatchmakingQueue
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinflipGlobalMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinflipGlobalSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.ExecutedApplicationCommandsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.InteractionsData
import net.perfectdreams.loritta.cinnamon.pudding.tables.Marriages
import net.perfectdreams.loritta.cinnamon.pudding.tables.ProfileDesignGroups
import net.perfectdreams.loritta.cinnamon.pudding.tables.ProfileDesigns
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.ServerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.Sets
import net.perfectdreams.loritta.cinnamon.pudding.tables.ShipEffects
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.TickerPrices
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserAchievements
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserSettings
import net.perfectdreams.loritta.cinnamon.pudding.utils.PuddingTasks
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.createOrUpdatePostgreSQLEnum
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.DEFAULT_REPETITION_ATTEMPTS
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager

class Pudding(private val database: Database) {
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
        fun createPostgreSQLPudding(address: String, databaseName: String, username: String, password: String): Pudding {
            val hikariConfig = createHikariConfig()
            hikariConfig.jdbcUrl = "jdbc:postgresql://$address/$databaseName"

            hikariConfig.username = username
            hikariConfig.password = password

            return Pudding(connectToDatabase(HikariDataSource(hikariConfig)))
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

            return hikariConfig
        }

        // Loritta (Legacy) uses this!
        fun connectToDatabase(dataSource: HikariDataSource): Database =
            Database.connect(
                HikariDataSource(dataSource),
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
    val executedApplicationCommandsLog = ExecutedApplicationCommandsLogService(this)
    val backgrounds = BackgroundsService(this)
    val profileDesigns = ProfileDesignsService(this)
    val bovespaBroker = BovespaBrokerService(this)
    val bets = BetsService(this)
    val puddingTasks = PuddingTasks(this)

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
            TickerPrices,
            BoughtStocks,
            BannedUsers,
            SonhosTransactionsLog,
            BrokerSonhosTransactionsLog,
            CachedDiscordUsers,
            CoinflipGlobalMatchmakingQueue,
            CoinflipGlobalMatchmakingResults,
            CoinflipGlobalSonhosTransactionsLog
        )

        if (schemas.isNotEmpty())
            transaction {
                createOrUpdatePostgreSQLEnum(AchievementType.values())
                createOrUpdatePostgreSQLEnum(ApplicationCommandType.values())
                createOrUpdatePostgreSQLEnum(LorittaBovespaBrokerUtils.BrokerSonhosTransactionsEntryAction.values())

                logger.info { "Tables to be created or updated: $schemas" }
                SchemaUtils.createMissingTablesAndColumns(
                    *schemas
                        .toMutableList()
                        .apply {
                            this.remove(ExecutedApplicationCommandsLog)
                        }.toTypedArray()
                )

                // This is a workaround because Exposed does not support (yet) Partitioned Tables
                if (ExecutedApplicationCommandsLog in schemas) {
                    // The reason we use partitioned tables for the ExecutedApplicationCommandsLog table, is because there is a LOT of commands there
                    // Removing old logs is painfully slow due to vacuuming and stuff, querying recent commands is also pretty slow.
                    // So it is better to split stuff up in separate partitions!
                    val createStatements = createStatementsPartitioned(ExecutedApplicationCommandsLog, "RANGE(sent_at)")

                    execStatements(false, createStatements)
                    commit()
                }

                // Now call the addMissingColumnsStatements again with the partitioned tables
                // We can not use createMissingTablesAndColumns here because Exposed will think that the table does not exist
                // because it is a partitioned table!
                if (ExecutedApplicationCommandsLog in schemas) {
                    val alterStatements = SchemaUtils.addMissingColumnsStatements(
                        ExecutedApplicationCommandsLog
                    )

                    execStatements(false, alterStatements)
                    commit()
                }
            }
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
    suspend fun <T> transaction(repetitions: Int = 5, statement: suspend org.jetbrains.exposed.sql.Transaction.() -> T): T {
        var lastException: Exception? = null
        for (i in 1..repetitions) {
            try {
                return org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction(Dispatchers.IO, database) {
                    statement.invoke(this)
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
    }
}