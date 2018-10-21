package com.mrpowergamerbr.loritta.network

import com.mrpowergamerbr.loritta.Loritta
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

object Databases {
	val hikariConfigLoritta by lazy {
		val config = HikariConfig()
		config.jdbcUrl = "jdbc:postgresql://${Loritta.config.postgreSqlIp}:${Loritta.config.postgreSqlPort}/${Loritta.config.databaseName}"
		config.username = Loritta.config.postgreSqlUser
		if (Loritta.config.postgreSqlPassword.isNotEmpty())
			config.password = Loritta.config.postgreSqlPassword
		config.driverClassName = "org.postgresql.Driver"

		config.maximumPoolSize = 256
		config.addDataSourceProperty("cachePrepStmts", "true")
		config.addDataSourceProperty("prepStmtCacheSize", "250")
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
		return@lazy config
	}
	val dataSourceLoritta by lazy { HikariDataSource(hikariConfigLoritta) }
	val loritta by lazy { Database.connect(dataSourceLoritta) }
}