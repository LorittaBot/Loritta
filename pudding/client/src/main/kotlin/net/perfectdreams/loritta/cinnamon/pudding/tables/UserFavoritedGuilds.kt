package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object UserFavoritedGuilds : LongIdTable() {
    val userId = long("user").index()
    val guildId = long("guild").index()
    val favoritedAt = timestampWithTimeZone("favorited_at")

    init {
        uniqueIndex(userId, guildId)
    }
}