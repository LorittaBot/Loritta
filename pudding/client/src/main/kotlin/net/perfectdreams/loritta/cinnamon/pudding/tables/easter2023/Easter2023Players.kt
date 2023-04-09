package net.perfectdreams.loritta.cinnamon.pudding.tables.easter2023

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.loritta.cinnamon.pudding.tables.SnowflakeTable

object Easter2023Players : SnowflakeTable() {
    val joinedAt = timestampWithTimeZone("joined_at")
}