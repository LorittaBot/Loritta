package net.perfectdreams.loritta.cinnamon.pudding.tables.transactions

import net.perfectdreams.exposedpowerutils.sql.postgresEnumeration
import net.perfectdreams.loritta.cinnamon.utils.DivineInterventionTransactionEntryAction
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import org.jetbrains.exposed.dao.id.LongIdTable

object DivineInterventionSonhosTransactionsLog : LongIdTable() {
    val timestampLog = reference("timestamp_log", SonhosTransactionsLog).index()
    val action = postgresEnumeration<DivineInterventionTransactionEntryAction>("action")
    val editedBy = optReference("edited_by", Profiles)
    val sonhos = long("sonhos")
    val reason = text("reason").nullable()
}