package net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object LoriCoolCardsSeenCards : LongIdTable() {
    val user = long("user").index()
    val card = reference("card", LoriCoolCardsEventCards).index()
    val seenAt = timestampWithTimeZone("seen_at")
}