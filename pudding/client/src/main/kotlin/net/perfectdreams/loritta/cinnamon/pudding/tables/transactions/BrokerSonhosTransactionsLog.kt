package net.perfectdreams.loritta.cinnamon.pudding.tables.transactions

import net.perfectdreams.exposedpowerutils.sql.postgresEnumeration
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaBovespaBrokerUtils
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.TickerPrices
import org.jetbrains.exposed.dao.id.LongIdTable

object BrokerSonhosTransactionsLog : LongIdTable() {
    val timestampLog = reference("timestamp_log", SonhosTransactionsLog).index()
    val action = postgresEnumeration<LorittaBovespaBrokerUtils.BrokerSonhosTransactionsEntryAction>("action")
    val ticker = reference("ticker", TickerPrices)
    val sonhos = long("sonhos")
    val stockPrice = long("stock_price")
    val stockQuantity = long("stock_quantity")
}