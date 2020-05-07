package net.perfectdreams.loritta.tables.servers.moduleconfigs

import org.jetbrains.exposed.dao.id.LongIdTable

object EconomyConfigs : LongIdTable() {
    val enabled = bool("enabled").default(false)
    val economyName = text("economy_name").nullable()
    val economyNamePlural = text("economy_name_plural").nullable()
    val sonhosExchangeEnabled = bool("sonhos_exchange_enabled").default(false)
    val exchangeRate = double("exchange_rate").nullable()
    val economyPurchaseEnabled = bool("economy_purchase_enabled").default(false)
    val realMoneyToEconomyRate = double("real_exchange_rate").nullable()
}