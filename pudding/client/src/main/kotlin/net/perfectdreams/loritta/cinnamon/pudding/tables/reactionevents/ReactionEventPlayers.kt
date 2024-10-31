package net.perfectdreams.loritta.cinnamon.pudding.tables.reactionevents

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object ReactionEventPlayers : LongIdTable() {
    val userId = long("user").index()
    val event = text("event").index()
    val joinedAt = timestampWithTimeZone("joined_at")
    val leftAt = timestampWithTimeZone("left_at").nullable()
}