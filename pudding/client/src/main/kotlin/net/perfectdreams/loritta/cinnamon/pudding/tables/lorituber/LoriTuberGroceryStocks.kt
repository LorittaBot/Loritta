package net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object LoriTuberGroceryStocks : LongIdTable() {
    val shopId = text("shop").index()
    val stockedAtTick = long("stocked_at_tick").index()
    val stockedAt = timestampWithTimeZone("stocked_at")
}