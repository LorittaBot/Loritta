package net.perfectdreams.loritta.dao

import net.perfectdreams.loritta.tables.MusicConfigs
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID

class MusicConfig(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, MusicConfig>(MusicConfigs)

	var enabled by MusicConfigs.enabled
	var channels by MusicConfigs.channels
}