package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.duration
import org.jetbrains.exposed.sql.javatime.timestamp

object CoinflipGlobalMatchmakingResults : LongIdTable() {
    val winner = reference("user", Profiles).index()
    val loser = reference("loser", Profiles).index()
    val quantity = long("quantity").index()
    val timestamp = timestamp("timestamp")
    val timeOnQueue = duration("time_on_queue")
}