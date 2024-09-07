package net.perfectdreams.loritta.cinnamon.pudding.tables.servers

import org.jetbrains.exposed.dao.id.LongIdTable

object GuildCommandConfigs : LongIdTable() {
    val guildId = long("guild").index()
    val commandId = uuid("command").index()
    val enabled = bool("enabled")

    init {
        uniqueIndex(guildId, commandId)
    }
}