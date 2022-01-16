package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.loritta.cinnamon.common.utils.SparklyPowerLSXTransactionEntryAction
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.postgresEnumeration
import org.jetbrains.exposed.dao.id.LongIdTable

object SparklyPowerLSXSonhosTransactionsLog : LongIdTable() {
    val timestampLog = reference("timestamp_log", SonhosTransactionsLog)
    val action = postgresEnumeration<SparklyPowerLSXTransactionEntryAction>("action")
    val sonhos = long("sonhos")
    val playerName = text("player_name")
    val playerUniqueId = uuid("player_unique_id")
    val exchangeRate = double("exchange_rate")
}