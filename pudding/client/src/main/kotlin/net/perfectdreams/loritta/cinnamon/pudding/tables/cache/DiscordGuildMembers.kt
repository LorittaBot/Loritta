package net.perfectdreams.loritta.cinnamon.pudding.tables.cache

import org.jetbrains.exposed.sql.Table

object DiscordGuildMembers : Table() {
    val guildId = long("guild").index()
    val userId = long("user").index()

    init {
        index(true, guildId, userId)
    }
}
