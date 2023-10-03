package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object AlwaysTrackTwitchAccounts : LongIdTable() {
    val userId = long("twitch_user_id").uniqueIndex()
    val addedAt = timestampWithTimeZone("added_at")
    val comment = text("comment")
}