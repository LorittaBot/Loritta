package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object BoughtStocks : LongIdTable() {
    val user = long("user").index()
    val ticker = reference("ticker", TickerPrices).index()
    val price = long("price")
    val boughtAt = long("bought_at")
}