package net.perfectdreams.loritta.cinnamon.pudding.tables.transactions

import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import org.jetbrains.exposed.dao.id.LongIdTable

object CoinFlipBetSonhosTransactionsLog : LongIdTable() {
    val timestampLog = reference("timestamp_log", SonhosTransactionsLog).index()
    val matchmakingResult = reference("matchmaking_result", CoinFlipBetMatchmakingResults)
}