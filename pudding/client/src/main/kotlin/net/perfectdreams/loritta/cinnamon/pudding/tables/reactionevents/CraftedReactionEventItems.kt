package net.perfectdreams.loritta.cinnamon.pudding.tables.reactionevents

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object CraftedReactionEventItems : LongIdTable() {
    val user = reference("user", ReactionEventPlayers).index()
    val event = text("event").index()
    val createdAt = timestampWithTimeZone("collected_at")
}