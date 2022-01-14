package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object CoinflipGlobalSonhosTransactionsLog : LongIdTable() {
    val timestampLog = reference("timestamp_log", SonhosTransactionsLog)
    val matchmakingResult = reference("matchmaking_result", CoinflipGlobalMatchmakingResults)
}