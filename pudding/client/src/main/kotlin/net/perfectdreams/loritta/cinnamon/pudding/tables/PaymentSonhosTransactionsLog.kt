package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object PaymentSonhosTransactionsLog : LongIdTable() {
    val timestampLog = reference("timestamp_log", SonhosTransactionsLog)
    val givenBy = reference("given_by", Profiles)
    val receivedBy = reference("received_by", Profiles)
    val sonhos = long("sonhos")
}