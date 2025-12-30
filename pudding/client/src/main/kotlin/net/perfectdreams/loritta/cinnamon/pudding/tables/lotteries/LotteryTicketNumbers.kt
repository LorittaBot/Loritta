package net.perfectdreams.loritta.cinnamon.pudding.tables.lotteries

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object LotteryTicketNumbers : LongIdTable() {
    val ticket = reference("ticket", LotteryTickets).index()
    val index = integer("index").index()
    val number = integer("number").index()
}