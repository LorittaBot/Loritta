package net.perfectdreams.loritta.cinnamon.pudding.tables.transactions

import net.perfectdreams.exposedpowerutils.sql.postgresEnumeration
import net.perfectdreams.loritta.cinnamon.common.utils.SparklyPowerLSXTransactionEntryAction
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import org.jetbrains.exposed.dao.id.LongIdTable

object SparklyPowerLSXSonhosTransactionsLog : LongIdTable() {
    val timestampLog = reference("timestamp_log", SonhosTransactionsLog).index()
    val action = postgresEnumeration<SparklyPowerLSXTransactionEntryAction>("action")
    val sonhos = long("sonhos")
    val sparklyPowerSonhos = long("sparklypower_sonhos")
    val playerName = text("player_name")
    val playerUniqueId = uuid("player_unique_id")
    val exchangeRate = double("exchange_rate")
}