package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object CoinFlipBetMatchmakingResults : LongIdTable() {
    val winner = reference("winner", Profiles).index()
    val loser = reference("loser", Profiles).index()
    val quantity = long("quantity").index()
    val quantityAfterTax = long("quantity_after_tax")
    val tax = long("tax").nullable()
    val taxPercentage = double("tax_percentage").nullable()
    val timestamp = timestampWithTimeZone("timestamp").index()
}