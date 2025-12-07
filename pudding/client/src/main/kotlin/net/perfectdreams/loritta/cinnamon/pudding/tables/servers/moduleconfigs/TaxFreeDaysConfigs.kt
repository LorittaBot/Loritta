package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import net.perfectdreams.loritta.cinnamon.pudding.tables.SnowflakeTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object TaxFreeDaysConfigs : SnowflakeTable() {
    val enabledDuringFriday = bool("enabled_during_friday").index()
    val enabledDuringSaturday = bool("enabled_during_saturday").index()
    val updatedAt = timestampWithTimeZone("updated_at").nullable()
}