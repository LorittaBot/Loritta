package net.perfectdreams.loritta.cinnamon.pudding.tables.cache

import org.jetbrains.exposed.dao.id.LongIdTable

object DiscordGuildMembers : LongIdTable() {
    val guildId = long("guild").index()
    val userId = long("user").index()

    init {
        index(true, guildId, userId)
    }
}
