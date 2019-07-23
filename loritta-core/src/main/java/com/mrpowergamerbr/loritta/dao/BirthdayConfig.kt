package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.tables.BirthdayConfigs
import com.mrpowergamerbr.loritta.tables.DonationConfigs
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID

class BirthdayConfig(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, BirthdayConfig>(DonationConfigs)

	var enabled by BirthdayConfigs.enabled
	var channelId by BirthdayConfigs.channelId
}