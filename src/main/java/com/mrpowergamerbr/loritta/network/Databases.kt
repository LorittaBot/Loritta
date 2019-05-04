package com.mrpowergamerbr.loritta.network

import com.mrpowergamerbr.loritta.Loritta
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.perfectdreams.loritta.utils.NetAddressUtils
import org.jetbrains.exposed.sql.Database

object Databases {
	val hikariConfigLoritta by lazy {
		val config = HikariConfig()
		config.jdbcUrl = "jdbc:postgresql://${NetAddressUtils.getWithPortIfMissing(Loritta.config.postgreSql.address, 5432)}/${Loritta.config.postgreSql.databaseName}"
		config.username = Loritta.config.postgreSql.username
		if (Loritta.config.postgreSql.password.isNotEmpty())
			config.password = Loritta.config.postgreSql.password
		config.driverClassName = "org.postgresql.Driver"

		config.maximumPoolSize = 24
		return@lazy config
	}
	val dataSourceLoritta by lazy { HikariDataSource(hikariConfigLoritta) }
	val loritta by lazy { Database.connect(dataSourceLoritta) }
}