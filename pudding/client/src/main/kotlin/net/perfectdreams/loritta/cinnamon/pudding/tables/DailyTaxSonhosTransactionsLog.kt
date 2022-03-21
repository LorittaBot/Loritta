package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object DailyTaxSonhosTransactionsLog : LongIdTable() {
    val timestampLog = reference("timestamp_log", SonhosTransactionsLog).index()
    val sonhos = long("sonhos")
    val maxDayThreshold = integer("max_day_threshold")
    val minimumSonhosForTrigger = long("minimum_sonhos_for_trigger")
    val tax = double("tax")
}