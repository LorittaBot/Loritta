package com.mrpowergamerbr.loritta.network

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.perfectdreams.loritta.utils.NetAddressUtils
import org.jetbrains.exposed.sql.Database
import java.io.File

object Databases {
	val hikariConfigLoritta by lazy {
		val loritta = com.mrpowergamerbr.loritta.utils.loritta

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
				dbPath = "//${NetAddressUtils.fixIp(NetAddressUtils.getWithPortIfMissing(loritta.config.database.address, 5432))}/${loritta.config.database.databaseName}"
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
		// Fixes connections not reconnecting automatically ~ https://github.com/brettwooldridge/HikariCP/issues/1056
		// 15 minutes
		config.addDataSourceProperty("socketTimeout", "900")

		// config.addDataSourceProperty("prepStmtCacheSize", "500")
		// config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
		// config.addDataSourceProperty("cachePrepStmts", "true")
		// config.addDataSourceProperty("useServerPrepStmts", "true")
		return@lazy config
	}
	val dataSourceLoritta by lazy { HikariDataSource(hikariConfigLoritta) }
	val loritta by lazy { Database.connect(dataSourceLoritta) }
}