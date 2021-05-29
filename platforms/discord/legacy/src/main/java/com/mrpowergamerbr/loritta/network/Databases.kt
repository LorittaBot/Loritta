package com.mrpowergamerbr.loritta.network

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.perfectdreams.loritta.utils.NetAddressUtils
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.io.File
import java.sql.Connection

object Databases {
	val hikariConfigLoritta by lazy {
		val loritta = com.mrpowergamerbr.loritta.utils.loritta

		val applicationName = loritta.lorittaCluster.getUserAgent()
		var jdbcUrlPrefix: String? = null
		var driverClassName: String? = null
		var dbPath: String? = null
		val sqLiteDbFile = File("loritta.db")

		val databaseType = loritta.config.database.type
		val config = HikariConfig()

		when (databaseType) {
			"SQLite" -> {
				jdbcUrlPrefix = "sqlite"
				driverClassName = "org.sqlite.JDBC"
				dbPath = "${sqLiteDbFile.toPath()}"
			}
			"SQLiteMemory" -> {
				jdbcUrlPrefix = "sqlite"
				driverClassName = "org.sqlite.JDBC"
				dbPath = "file:loritta?mode=memory&cache=shared"
				config.addDataSourceProperty("cache", "shared")
			}
			"PostgreSQL" -> {
				jdbcUrlPrefix = "postgresql"
				driverClassName = "org.postgresql.Driver"
				dbPath = "//${NetAddressUtils.fixIp(NetAddressUtils.getWithPortIfMissing(loritta.config.database.address, 5432))}/${loritta.config.database.databaseName}?ApplicationName=$applicationName"
			}
			"PGJDBC-NG" -> {
				jdbcUrlPrefix = "pgsql"
				driverClassName = "com.impossibl.postgres.jdbc.PGDriver"
				dbPath = "//${NetAddressUtils.fixIp(NetAddressUtils.getWithPortIfMissing(loritta.config.database.address, 5432))}/${loritta.config.database.databaseName}"
			}
			else -> throw RuntimeException("Unsupported Database Dialect $databaseType")
		}

		config.jdbcUrl = "jdbc:$jdbcUrlPrefix:$dbPath"
		config.username = loritta.config.database.username
		if (loritta.config.database.password.isNotEmpty())
			config.password = loritta.config.database.password
		config.driverClassName = driverClassName

		config.maximumPoolSize = LorittaLauncher.loritta.config.database.maximumPoolSize
		config.minimumIdle = LorittaLauncher.loritta.config.database.minimumIdle

		// https://github.com/JetBrains/Exposed/wiki/DSL#batch-insert
		config.addDataSourceProperty("reWriteBatchedInserts", "true")

		// Exposed uses autoCommit = false, so we need to set this to false to avoid HikariCP resetting the connection to
		// autoCommit = true when the transaction goes back to the pool, because resetting this has a "big performance impact"
		// https://stackoverflow.com/a/41206003/7271796
		config.isAutoCommit = false

		// Useful to check if a connection is not returning to the pool, will be shown in the log as "Apparent connection leak detected"
		config.leakDetectionThreshold = 30 * 1000

		// We need to use the same transaction isolation used in Exposed, in this case, TRANSACTION_READ_COMMITED.
		// If not HikariCP will keep resetting to the default when returning to the pool, causing performance issues.
		if (!loritta.config.database.type.startsWith("SQLite")) // SQLite only supports TRANSACTION_SERIALIZABLE and TRANSACTION_READ_UNCOMMITTED.
			config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"

		return@lazy config
	}
	val dataSourceLoritta by lazy { HikariDataSource(hikariConfigLoritta) }
	val loritta by lazy {
		val loritta = com.mrpowergamerbr.loritta.utils.loritta

		val database = Database.connect(dataSourceLoritta)

		// Exposed 0.28.1 changed the transaction isolation level to Connection.TRANSACTION_READ_COMMITED
		// So we switch back to our good old reliable TRANSACTION_REPETABLE_READ to *trigger* concurrent update exceptions.
		// Because we don't want our stuff to be overwritten by other concurrent queries!
		if (!loritta.config.database.type.startsWith("SQLite")) // SQLite only supports TRANSACTION_SERIALIZABLE and TRANSACTION_READ_UNCOMMITTED.
			TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ

		database
	}
}