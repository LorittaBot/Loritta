package net.perfectdreams.loritta.cinnamon.pudding.tables.transactions

import net.perfectdreams.loritta.cinnamon.pudding.tables.Dailies
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import org.jetbrains.exposed.dao.id.LongIdTable

object DailyRewardSonhosTransactionsLog: LongIdTable() {
    val timestampLog = reference("timestamp_log", SonhosTransactionsLog).index()
    val quantity = long("quantity")
    val daily = reference("daily", Dailies)
}