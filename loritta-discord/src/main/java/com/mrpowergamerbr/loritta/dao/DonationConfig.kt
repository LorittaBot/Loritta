package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.tables.DonationConfigs
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class DonationConfig(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, DonationConfig>(DonationConfigs)

	var customBadge by DonationConfigs.customBadge
	var dailyMultiplier by DonationConfigs.dailyMultiplier
}