package net.perfectdreams.pudding

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import net.perfectdreams.dora.tables.CachedDiscordUserIdentifications
import net.perfectdreams.dora.tables.LanguageTargets
import net.perfectdreams.dora.tables.ProjectUserPermissions
import net.perfectdreams.dora.tables.Projects
import net.perfectdreams.dora.tables.SourceStrings
import net.perfectdreams.dora.tables.TranslationsStrings
import net.perfectdreams.dora.tables.UserWebsiteSessions
import net.perfectdreams.dora.tables.Users
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.thread

// A "barebones" alternative to Loritta's Pudding class
class Pudding(
    val currentSchemaVersion: Int,
    val hikariDataSource: HikariDataSource,
    val database: Database,
    private val cachedThreadPool: ExecutorService,
    val dispatcher: CoroutineDispatcher,
    permits: Int
) {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
        private val DRIVER_CLASS_NAME = "org.postgresql.Driver"
        private val ISOLATION_LEVEL = IsolationLevel.TRANSACTION_REPEATABLE_READ // We use repeatable read to avoid dirty and non-repeatable reads! Very useful and safe!!
        private val SCHEMA_ID = UUID.fromString("600556aa-2920-41c7-b26c-7717eff2d392") // This is a random unique ID, it is used for upserting the schema version
        private val lockId = "pudding-schema-updater".hashCode()

        /**
         * Creates a Pudding instance backed by a PostgreSQL database
         *
         * @param schemaVersion the version of the schema, used for migrations
         * @param postgreSqlApplicationName the name of the application, shown in PostgreSQL logs
         * @param address      the PostgreSQL address
         * @param databaseName the database name in PostgreSQL
         * @param username     the PostgreSQL username
         * @param password     the PostgreSQL password
         * @return a [Flannel] instance backed by a PostgreSQL database
         */
        fun createPostgreSQL(
            schemaVersion: Int,
            postgreSqlApplicationName: String,
            address: String,
            databaseName: String,
            username: String,
            password: String,
            permits: Int = 128,
            builder: HikariConfig.() -> (Unit) = {}
        ): Pudding {
            val hikariConfig = createHikariConfig(builder)
            hikariConfig.jdbcUrl = "jdbc:postgresql://$address/$databaseName?ApplicationName=$postgreSqlApplicationName"

            hikariConfig.username = username
            hikariConfig.password = password

            val hikariDataSource = HikariDataSource(hikariConfig)

            val cachedThreadPool = Executors.newCachedThreadPool()

            return Pudding(
                schemaVersion,
                hikariDataSource,
                connectToDatabase(hikariDataSource),
                cachedThreadPool,
                // Instead of using Dispatchers.IO directly, we will create a cached thread pool.
                // This avoids issues when all Dispatchers.IO threads are blocked on transactions, causing any other coroutine using the Dispatcher.IO job to be
                // blocked.
                // Example: 64 blocked coroutines due to transactions (64 = max threads in a Dispatchers.IO dispatcher) + you also have a WebSocket listening for events, when the WS tries to
                // read incoming events, it is blocked because there isn't any available Dispatchers.IO threads!
                cachedThreadPool.asCoroutineDispatcher(),
                permits
            )
        }

        private fun createHikariConfig(builder: HikariConfig.() -> (Unit)): HikariConfig {
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

            hikariConfig.maximumPoolSize = 16
            hikariConfig.poolName = "PuddingPool"

            hikariConfig.apply(builder)

            return hikariConfig
        }

        // Loritta (Legacy) uses this!
        @OptIn(ExperimentalKeywordApi::class)
        fun connectToDatabase(dataSource: HikariDataSource): Database =
            Database.connect(
                dataSource,
                databaseConfig = DatabaseConfig {
                    this.defaultMaxAttempts = 5
                    defaultIsolationLevel = ISOLATION_LEVEL.levelId // Change our default isolation level
                    // "A table that is created with a keyword identifier now logs a warning that the identifier's case may be lost when it is automatically quoted in generated SQL."
                    // "DatabaseConfig now includes the property preserveKeywordCasing, which can be set to true to remove these warnings and to ensure that the identifier matches the exact case used."
                    // Our tables are all in lowercase because that's what the default Exposed behavior was, so we need to set this to false
                    preserveKeywordCasing = false
                }
            )
    }

    // Used to avoid having a lot of threads being created on the "dispatcher" just to be blocked waiting for a connection, causing thread starvation and an OOM kill
    val semaphore = Semaphore(permits)

    /**
     * Creates missing tables and columns in the database.
     *
     * To avoid updating tables that are being used by legacy applications (Loritta Legacy), the [shouldBeUpdated] parameter
     * can be used to filter what tables should be created.
     *
     * **Synchronization should be handled by something external, to avoid migrations being ran when two instances attempt to migrate it!**
     *
     * @param shouldBeUpdated checks if a table should be created/updated or not, if true, it will be included to be updated, if false, it won't
     */
    suspend fun runMigrations() {
        // Validate that all migration scripts are present
        /* for (i in 1.. currentSchemaVersion) {
            val migrationScript = Mairin::class.java.getResourceAsStream("/migrations/${i.toString().padStart(5, '0')}.sql")
            if (migrationScript == null)
                error("Missing migration script for version $i!")
        } */

        // We did try to use advisory locks to handle duplicated migrations, but that didn't work due to race conditions.
        // If we started a transaction, there was a small time frame that the transaction "looked" at the old schema version.
        transaction {
            SchemaUtils.create(
                SchemaVersion,
                UserWebsiteSessions,
                CachedDiscordUserIdentifications,
                SourceStrings,
                TranslationsStrings,
                LanguageTargets,
                Projects,
                Users,
                ProjectUserPermissions
            )

            var databaseSchemaVersion: Int? = null
            val schemaVersionOverride = Integer.getInteger("loritta.schemaVersionOverride", null)
            if (schemaVersionOverride != null) {
                logger.info { "Overriding Schema Version to $schemaVersionOverride" }
                databaseSchemaVersion = schemaVersionOverride
            } else {
                if (checkIfTableExists(SchemaVersion)) {
                    val schemaVersion = SchemaVersion.select(SchemaVersion.version).where { SchemaVersion.id eq SCHEMA_ID }
                        .firstOrNull()
                        ?.get(SchemaVersion.version)

                    if (schemaVersion == currentSchemaVersion) {
                        logger.info { "Database schema version matches (database: ${schemaVersion}; schema: $currentSchemaVersion), so we won't update any tables, yay!" }
                        return@transaction true
                    } else {
                        if (schemaVersion != null && schemaVersion > currentSchemaVersion) {
                            logger.warn { "Database schema version is newer (database: ${schemaVersion}; schema: $currentSchemaVersion), so we will not update the tables to avoid issues..." }
                            return@transaction true
                        } else {
                            logger.info { "Database schema version is older (database: ${schemaVersion}; schema: $currentSchemaVersion), so we will update the tables, yay!" }
                            databaseSchemaVersion = schemaVersion
                        }
                    }
                } else {
                    logger.warn { "SchemaVersion doesn't seem to exist! Pretending that the current schema version is 0..." }
                    databaseSchemaVersion = 0
                }
            }

            if (databaseSchemaVersion != null) {
                logger.info { "Running migration scripts in order..." }
                for (upgradeVersion in databaseSchemaVersion + 1..currentSchemaVersion) {
                    logger.info { "Updating database schema version to $upgradeVersion..." }

                    val migrationScript = Pudding::class.java.getResourceAsStream("/migrations/${upgradeVersion.toString().padStart(5, '0')}.sql")

                    if (migrationScript != null) {
                        val script = migrationScript.readBytes().toString(Charsets.UTF_8)

                        val statement = this.connection.prepareStatement(script, false)
                        statement.executeUpdate()
                    } else {
                        logger.info { "Version $upgradeVersion does not have a migration version!" }
                    }

                    SchemaVersion.upsert(SchemaVersion.id) {
                        it[id] = SCHEMA_ID
                        it[version] = upgradeVersion
                    }
                }
            } else {
                // If the schema version is null, then we don't need to execute the migrations scripts
                // But we still need to upsert the current schema version!
                SchemaVersion.upsert(SchemaVersion.id) {
                    it[id] = SCHEMA_ID
                    it[version] = currentSchemaVersion
                }
            }

            logger.info { "All migrations were successfully applied! Releasing advisory lock..." }
            return@transaction true
        }
    }

    private fun Transaction.createPartitionedTable(table: Table) {
        // The reason we use partitioned tables for the ExecutedApplicationCommandsLog table, is because there is a LOT of commands there
        // Removing old logs is painfully slow due to vacuuming and stuff, querying recent commands is also pretty slow.
        // So it is better to split stuff up in separate partitions!
        val createStatements = createStatementsPartitioned(table, "RANGE(sent_at)")

        execStatements(false, createStatements)
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
        val schema = tableScheme?.inProperCase() ?: TransactionManager.current().connection.metadata {
            // TODO: I'm not sure how to correctly get the schema names, before we used "this.currentScheme" but that's has since been removed
            // The result of "schemaNames" is [information_schema, pg_catalog, public]
            // We could hardcode the "public" result, but let's throw an error if it isn't found
            this.schemaNames.firstOrNull { it == "public" } ?: error("Missing \"public\" schema")
        }
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

    suspend fun <T> transaction(repetitions: Int = 5, transactionIsolation: Int? = null, statement: suspend Transaction.() -> T) = suspendableNestableTransaction(
        dispatcher,
        database,
        repetitions,
        transactionIsolation,
        {
            semaphore.withPermit {
                it.invoke()
            }
        },
        statement
    )

    fun shutdown() {
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
