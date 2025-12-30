package net.perfectdreams.loritta.cinnamon.pudding.tables.lotteries

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object LotteryTickets : LongIdTable() {
    val userId = long("user").index()
    val lottery = reference("lottery", Lotteries).index()
    val boughtAt = timestampWithTimeZone("bought_at")
    val winner = bool("winner").index()
}