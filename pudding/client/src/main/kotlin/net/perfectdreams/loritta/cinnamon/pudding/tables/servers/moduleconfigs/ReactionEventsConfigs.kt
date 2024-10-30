package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import net.perfectdreams.loritta.cinnamon.pudding.tables.SnowflakeTable

object ReactionEventsConfigs : SnowflakeTable() {
    val enabled = bool("enabled")
}