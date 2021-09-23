package net.perfectdreams.loritta.cinnamon.pudding

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import kotlinx.coroutines.Dispatchers
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.achievements.AchievementType
import net.perfectdreams.loritta.cinnamon.pudding.services.InteractionsDataService
import net.perfectdreams.loritta.cinnamon.pudding.services.MarriagesService
import net.perfectdreams.loritta.cinnamon.pudding.services.ServerConfigsService
import net.perfectdreams.loritta.cinnamon.pudding.services.ShipEffectsService
import net.perfectdreams.loritta.cinnamon.pudding.services.SonhosService
import net.perfectdreams.loritta.cinnamon.pudding.services.UsersService
import net.perfectdreams.loritta.cinnamon.pudding.tables.Backgrounds
import net.perfectdreams.loritta.cinnamon.pudding.tables.InteractionsData
import net.perfectdreams.loritta.cinnamon.pudding.tables.Marriages
import net.perfectdreams.loritta.cinnamon.pudding.tables.ProfileDesigns
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.ServerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.Sets
import net.perfectdreams.loritta.cinnamon.pudding.tables.ShipEffects
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserAchievements
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserSettings
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.createOrUpdatePostgreSQLEnum
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.DEFAULT_REPETITION_ATTEMPTS
import org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManager

open class Pudding(private val database: Database) {
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

        /**
         * Creates a Pudding instance backed by a PostgreSQL database, the database will be stored in the [path] directory
         *
         * @return a [Pudding] instance backed by a PostgreSQL database
         */
        fun createEmbeddedPostgreSQLPudding(path: String): Pudding {
            val embeddedPostgres = EmbeddedPostgres.builder()
                .setCleanDataDirectory(false)
                .setDataDirectory(path)
                .start()

            val hikariConfig = createHikariConfig()
            hikariConfig.dataSource = embeddedPostgres.postgresDatabase

            return EmbeddedPSQLPudding(
                embeddedPostgres,
                connectToDatabase(HikariDataSource(hikariConfig))
            )
        }

        /**
         * Creates a Pudding instance backed by a PostgreSQL database, the database will be stored in temporary files, so it isn't persistent
         *
         * @return a [Pudding] instance backed by a PostgreSQL database
         */
        fun createMemoryPostgreSQLPudding(): Pudding {
            val embeddedPostgres = EmbeddedPostgres.builder()
                .start()

            val hikariConfig = createHikariConfig()
            hikariConfig.dataSource = embeddedPostgres.postgresDatabase

            return EmbeddedPSQLPudding(
                embeddedPostgres,
                connectToDatabase(HikariDataSource(hikariConfig))
            )
        }

        private fun createHikariConfig(): HikariConfig {
            val hikariConfig = HikariConfig()

            hikariConfig.driverClassName = DRIVER_CLASS_NAME

            // Exposed uses autoCommit = false, so we need to set this to false to avoid HikariCP resetting the connection to
            // autoCommit = true when the transaction goes back to the pool, because resetting this has a "big performance impact"
            // https://stackoverflow.com/a/41206003/7271796
            hikariConfig.isAutoCommit = false

            // Useful to check if a connection is not returning to the pool, will be shown in the log as "Apparent connection leak detected"
            hikariConfig.leakDetectionThreshold = 30L * 1000
            hikariConfig.transactionIsolation = IsolationLevel.TRANSACTION_REPEATABLE_READ.name // We use repeatable read to avoid dirty and non-repeatable reads! Very useful and safe!!

            return hikariConfig
        }

        private fun connectToDatabase(dataSource: HikariDataSource): Database =
            Database.connect(
                HikariDataSource(dataSource)
            ) {
                // This code is the same callback used in the "Database.connect(...)" call, but with the default isolation level change
                ThreadLocalTransactionManager(it, DEFAULT_REPETITION_ATTEMPTS).also {
                    it.defaultIsolationLevel = ISOLATION_LEVEL.levelId // Change our default isolation level
                }
            }
    }

    val users = UsersService(this)
    val serverConfigs = ServerConfigsService(this)
    val sonhos = SonhosService(this)
    val shipEffects = ShipEffectsService(this)
    val marriages = MarriagesService(this)
    val interactionsData = InteractionsDataService(this)

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
        fun insertIfValid(vararg tables: Table) = schemas.addAll(tables.filter { shouldBeUpdated.invoke(it::class.simpleName!!) })
        insertIfValid(
            Sets,
            ProfileDesigns,
            Backgrounds,
            UserSettings,
            Profiles,
            ServerConfigs,
            ShipEffects,
            Marriages,
            UserAchievements,
            InteractionsData
        )

        if (schemas.isNotEmpty())
            transaction {
                createOrUpdatePostgreSQLEnum(AchievementType.values())

                SchemaUtils.createMissingTablesAndColumns(
                    *schemas.toTypedArray()
                )
            }
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

    open fun shutdown() {}
}