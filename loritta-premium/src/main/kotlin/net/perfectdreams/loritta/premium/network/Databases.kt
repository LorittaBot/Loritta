package net.perfectdreams.loritta.premium.network

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.perfectdreams.loritta.premium.LorittaPremium
import net.perfectdreams.loritta.utils.NetAddressUtils
import org.jetbrains.exposed.sql.Database

object Databases {
	val hikariConfigLoritta by lazy {
		val loritta = LorittaPremium.INSTANCE
		val config = HikariConfig()
		config.jdbcUrl = "jdbc:postgresql://${NetAddressUtils.getWithPortIfMissing(loritta.config.postgreSql.address, 5432)}/${loritta.config.postgreSql.databaseName}"
		config.username = loritta.config.postgreSql.username
		if (loritta.config.postgreSql.password.isNotEmpty())
			config.password = loritta.config.postgreSql.password
		config.driverClassName = "org.postgresql.Driver"

		config.maximumPoolSize = loritta.config.postgreSql.maximumPoolSize
		return@lazy config
	}
	val dataSourceLoritta by lazy { HikariDataSource(hikariConfigLoritta) }
	val loritta by lazy { Database.connect(dataSourceLoritta) }
}