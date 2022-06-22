package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils

import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

class QueueDatabase(
    val database: Database,
    val hikariDataSource: HikariDataSource
)