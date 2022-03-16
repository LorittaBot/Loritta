package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.loritta.cinnamon.common.utils.DivineInterventionTransactionEntryAction
import net.perfectdreams.exposedpowerutils.sql.postgresEnumeration
import org.jetbrains.exposed.dao.id.LongIdTable

object DivineInterventionSonhosTransactionsLog : LongIdTable() {
    val timestampLog = reference("timestamp_log", SonhosTransactionsLog)
    val action = postgresEnumeration<DivineInterventionTransactionEntryAction>("action")
    val editedBy = optReference("edited_by", Profiles)
    val sonhos = long("sonhos")
    val reason = text("reason").nullable()
}