package net.perfectdreams.loritta.cinnamon.pudding.tables.lotteries

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object Lotteries : LongIdTable() {
    val startedAt = timestampWithTimeZone("started_at").index()
    val endsAt = timestampWithTimeZone("ends_at").index()
    val endedAt = timestampWithTimeZone("ended_at").nullable().index()
    val tableTotalNumbers = integer("table_total_numbers")
    val numbersPerTicket = integer("numbers_per_ticket")
    val ticketPrice = long("ticket_price")
    val winningNumbers = array<Int>("winning_numbers").nullable()
    val hits = integer("hits").nullable()
    val houseSponsorship = long("house_sponsorship").nullable()
}