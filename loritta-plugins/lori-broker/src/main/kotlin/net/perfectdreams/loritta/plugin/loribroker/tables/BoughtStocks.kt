package net.perfectdreams.loritta.plugin.loribroker.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object BoughtStocks : LongIdTable() {
    val user = long("user").index()
    val ticker = text("ticker").index()
    val price = long("price")
    val boughtAt = long("bought_at")
}