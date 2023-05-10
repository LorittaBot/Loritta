package net.perfectdreams.loritta.cinnamon.pudding.tables.raffles

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.exposedpowerutils.sql.postgresEnumeration
import net.perfectdreams.loritta.common.utils.RaffleType
import org.jetbrains.exposed.dao.id.LongIdTable

object Raffles : LongIdTable() {
    val startedAt = timestampWithTimeZone("started_at").index()
    val endsAt = timestampWithTimeZone("ends_at").index()
    val endedAt = timestampWithTimeZone("ended_at").nullable().index()
    val raffleType = postgresEnumeration<RaffleType>("type")

    val winnerTicket = optReference("winner_ticket", RaffleTickets)
    val paidOutPrize = long("paid_out_prize").nullable()
}