package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object CoinFlipBetSonhosTransactionsLog : LongIdTable() {
    val timestampLog = reference("timestamp_log", SonhosTransactionsLog).index()
    val matchmakingResult = reference("matchmaking_result", CoinFlipBetMatchmakingResults)
}