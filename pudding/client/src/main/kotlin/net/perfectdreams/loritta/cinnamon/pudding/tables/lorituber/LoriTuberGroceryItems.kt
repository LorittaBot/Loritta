package net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object LoriTuberGroceryItems : LongIdTable() {
    val item = text("item")
    val storeStock = reference("stocks", LoriTuberGroceryStocks)
    val boughtBy = optReference("bought_by", LoriTuberCharacters).index()
    val boughtAt = timestampWithTimeZone("bought_at").nullable()
}