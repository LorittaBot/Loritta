package net.perfectdreams.loritta.cinnamon.pudding.tables.christmas2022

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.loritta.cinnamon.pudding.tables.SnowflakeTable

object Christmas2022Players : SnowflakeTable() {
    val joinedAt = timestampWithTimeZone("joined_at")
}