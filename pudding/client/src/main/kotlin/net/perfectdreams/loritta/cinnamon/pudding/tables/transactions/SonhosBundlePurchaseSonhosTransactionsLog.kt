package net.perfectdreams.loritta.cinnamon.pudding.tables.transactions

import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosBundles
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import org.jetbrains.exposed.dao.id.LongIdTable

object SonhosBundlePurchaseSonhosTransactionsLog : LongIdTable() {
    val timestampLog = reference("timestamp_log", SonhosTransactionsLog).index()
    val bundle = reference("bundle", SonhosBundles)
}