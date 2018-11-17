package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.tables.RegisterConfigs
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID

class RegisterConfig(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, RegisterConfig>(RegisterConfigs)

	var holder by RegisterConfigs.holder
}