package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object EmojiFightParticipants : LongIdTable() {
    val user = reference("user", Profiles).index()
    val match = reference("match", EmojiFightMatches)
    var emoji = text("emoji")
}