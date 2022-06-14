package net.perfectdreams.loritta.cinnamon.pudding.tables.transactions

import net.perfectdreams.loritta.cinnamon.pudding.tables.PaymentSonhosTransactionResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import org.jetbrains.exposed.dao.id.LongIdTable

object PaymentSonhosTransactionsLog : LongIdTable() {
    val timestampLog = reference("timestamp_log", SonhosTransactionsLog).index()
    val paymentResult = reference("payment_result", PaymentSonhosTransactionResults)
}