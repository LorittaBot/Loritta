package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object EmojiFightMatches : LongIdTable() {
    val createdBy = reference("created_by", Profiles).index()
    val createdAt = timestamp("created_at")
    val finishedAt = timestamp("finished_at")
    val maxPlayers = integer("max_players")
}