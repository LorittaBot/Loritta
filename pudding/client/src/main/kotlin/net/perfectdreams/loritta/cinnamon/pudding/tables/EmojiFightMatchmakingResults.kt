package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object EmojiFightMatchmakingResults : LongIdTable() {
    val winner = reference("winner", EmojiFightParticipants).index()
    val entryPrice = long("entry_price")
    val entryPriceAfterTax = long("entry_price_after_tax")
    val tax = long("tax").nullable()
    val taxPercentage = double("tax_percentage").nullable()
    val match = optReference("match", EmojiFightMatches)
}