package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.sql.ReferenceOption

object ServerConfigs : SnowflakeTable() {
    val localeId = text("locale_id").default("default")
    val starboardConfig = optReference("starboard_config", StarboardConfigs, onDelete = ReferenceOption.CASCADE).index()
    val miscellaneousConfig = optReference("miscellaneous_config", MiscellaneousConfigs, onDelete = ReferenceOption.CASCADE).index()
}