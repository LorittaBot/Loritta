package net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.exposedpowerutils.sql.jsonb
import org.jetbrains.exposed.dao.id.LongIdTable

object LoriCoolCardsUserTrades : LongIdTable() {
    val user1 = long("user1").index()
    val user2 = long("user2").index()
    val event = reference("event", LoriCoolCardsEvents).index()
    val tradedAt = timestampWithTimeZone("traded_at")
    val tradeOffer = jsonb("trade_offer")
}