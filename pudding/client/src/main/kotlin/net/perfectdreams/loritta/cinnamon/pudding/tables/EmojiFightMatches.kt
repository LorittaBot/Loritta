package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object EmojiFightMatches : LongIdTable() {
    val createdBy = reference("created_by", Profiles).index()
    val createdAt = timestampWithTimeZone("created_at")
    val finishedAt = timestampWithTimeZone("finished_at")
    val maxPlayers = integer("max_players")
}