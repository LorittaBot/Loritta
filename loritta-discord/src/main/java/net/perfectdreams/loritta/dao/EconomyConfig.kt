package net.perfectdreams.loritta.dao

import net.perfectdreams.loritta.tables.EconomyConfigs
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID

class EconomyConfig(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, EconomyConfig>(EconomyConfigs)

	var enabled by EconomyConfigs.enabled
	var economyName by EconomyConfigs.economyName
	var economyNamePlural by EconomyConfigs.economyNamePlural

	var sonhosExchangeEnabled by EconomyConfigs.sonhosExchangeEnabled
	var exchangeRate by EconomyConfigs.exchangeRate
	var economyPurchaseEnabled by EconomyConfigs.economyPurchaseEnabled
	var realMoneyToEconomyRate by EconomyConfigs.realMoneyToEconomyRate
}