package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object MarriageParticipants : LongIdTable() {
    val user = long("user").index()
    val marriage = reference("marriage", UserMarriages).index()
    val joinedAt = timestampWithTimeZone("joined_at")
    val primaryMarriage = bool("primary_marriage").index()
}