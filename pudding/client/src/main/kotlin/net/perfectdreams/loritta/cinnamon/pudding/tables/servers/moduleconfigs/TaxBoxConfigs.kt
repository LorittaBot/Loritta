package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import net.perfectdreams.loritta.cinnamon.pudding.tables.SnowflakeTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object TaxBoxConfigs : SnowflakeTable() {
    val enabled = bool("enabled").index()
    val updatedAt = timestampWithTimeZone("updated_at").nullable()
}