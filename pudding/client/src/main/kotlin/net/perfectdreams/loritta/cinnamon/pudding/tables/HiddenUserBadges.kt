package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object HiddenUserBadges : LongIdTable() {
    val userId = long("user").index()
    val badgeId = uuid("badge_id").index()
    val hiddenAt = timestampWithTimeZone("hidden_at")

    init {
        uniqueIndex(userId, badgeId)
    }
}