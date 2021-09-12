package net.perfectdreams.loritta.cinnamon.pudding.tables

object ServerConfigs : SnowflakeTable() {
    val localeId = text("locale_id").default("default")
}