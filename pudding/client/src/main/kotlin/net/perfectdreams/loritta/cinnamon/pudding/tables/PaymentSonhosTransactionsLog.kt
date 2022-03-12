package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object PaymentSonhosTransactionsLog : LongIdTable() {
    val timestampLog = reference("timestamp_log", SonhosTransactionsLog)
    val paymentResult = reference("payment_result", PaymentSonhosTransactionResults)
}