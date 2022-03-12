package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object SonhosBundlePurchaseSonhosTransactionsLog : LongIdTable() {
    val timestampLog = reference("timestamp_log", SonhosTransactionsLog)
    val bundle = reference("bundle", SonhosBundles)
}