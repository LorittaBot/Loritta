package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object TickerPrices : IdTable<String>() {
    val ticker = text("ticker").entityId()
    val status = text("status").index()
    val value = long("value")
    val dailyPriceVariation = double("daily_price_variation")
    val lastUpdatedAt = timestamp("last_updated_at")
    val enabled = bool("enabled").default(true)

    override val id = ticker
    override val primaryKey = PrimaryKey(id)
}