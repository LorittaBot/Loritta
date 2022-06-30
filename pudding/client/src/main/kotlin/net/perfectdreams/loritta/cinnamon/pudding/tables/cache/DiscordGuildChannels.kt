package net.perfectdreams.loritta.cinnamon.pudding.tables.cache

import org.jetbrains.exposed.dao.id.LongIdTable

object DiscordGuildChannels : LongIdTable() {
    val guildId = long("guild").index()
    val channelId = long("channel").index()
    val name = text("name").nullable()
    val type = integer("type")
    val permissions = long("permissions").nullable()

    init {
        index(true, guildId, channelId)
    }
}
