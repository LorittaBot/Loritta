package net.perfectdreams.loritta.dao

import net.perfectdreams.loritta.tables.LevelConfigs
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID

class LevelConfig(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, LevelConfig>(LevelConfigs)

	var roleGiveType by LevelConfigs.roleGiveType
	var noXpRoles by LevelConfigs.noXpRoles
	var noXpChannels by LevelConfigs.noXpChannels
}