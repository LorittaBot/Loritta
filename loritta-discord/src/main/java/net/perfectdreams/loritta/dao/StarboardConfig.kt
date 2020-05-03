package net.perfectdreams.loritta.dao

import net.perfectdreams.loritta.tables.StarboardConfigs
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID

class StarboardConfig(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, StarboardConfig>(StarboardConfigs)

	var enabled by StarboardConfigs.enabled
	var starboardChannelId by StarboardConfigs.starboardChannelId
	var requiredStars by StarboardConfigs.requiredStars
}