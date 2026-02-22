package net.perfectdreams.loritta.cinnamon.pudding.tables.raffles

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object RaffleTickets : LongIdTable() {
    val userId = long("user").index()
    val raffle = reference("raffle", Raffles).index()
    val boughtAt = timestampWithTimeZone("bought_at")
    val boughtTickets = long("bought_tickets").default(1)
}