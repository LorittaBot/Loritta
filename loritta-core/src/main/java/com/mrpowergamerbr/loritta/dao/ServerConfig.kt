package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.tables.ServerConfigs
import net.perfectdreams.loritta.dao.EconomyConfig
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID

class ServerConfig(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, ServerConfig>(ServerConfigs)

	val guildId = this.id.value
	var donationKey by DonationKey optionalReferencedOn ServerConfigs.donationKey
	var donationConfig by DonationConfig optionalReferencedOn ServerConfigs.donationConfig
	var birthdayConfig by BirthdayConfig optionalReferencedOn ServerConfigs.birthdayConfig
	var economyConfig by EconomyConfig optionalReferencedOn ServerConfigs.economyConfig
}