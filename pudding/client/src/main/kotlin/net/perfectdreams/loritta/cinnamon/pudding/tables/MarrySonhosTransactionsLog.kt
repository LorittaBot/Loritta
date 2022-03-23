package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.loritta.cinnamon.pudding.tables.DivineInterventionSonhosTransactionsLog.index
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object MarrySonhosTransactionsLog : LongIdTable() {
    val timestampLog = reference("timestamp_log", SonhosTransactionsLog).index()
    val user = reference("user", Profiles).index()
    val partner = reference("partner", Profiles)
    val sonhos = long("sonhos")
}