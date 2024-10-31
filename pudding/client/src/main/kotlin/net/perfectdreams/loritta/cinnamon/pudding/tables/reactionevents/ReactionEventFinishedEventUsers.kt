package net.perfectdreams.loritta.cinnamon.pudding.tables.reactionevents

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object ReactionEventFinishedEventUsers : LongIdTable() {
    val user = reference("user", ReactionEventPlayers)
    val event = text("event").index()
    val finishedAt = timestampWithTimeZone("finished_at")
}