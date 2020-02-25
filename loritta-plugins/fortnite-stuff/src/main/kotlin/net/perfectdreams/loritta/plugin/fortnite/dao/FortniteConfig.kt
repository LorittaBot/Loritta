package net.perfectdreams.loritta.plugin.fortnite.dao

import net.perfectdreams.loritta.plugin.fortnite.tables.FortniteConfigs
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID

class FortniteConfig(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, FortniteConfig>(FortniteConfigs)

	var advertiseNewItems by FortniteConfigs.advertiseNewItems
	var channelToAdvertiseNewItems by FortniteConfigs.channelToAdvertiseNewItems
}