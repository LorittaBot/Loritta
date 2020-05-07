package net.perfectdreams.loritta.dao.servers.moduleconfigs

import net.perfectdreams.loritta.tables.servers.moduleconfigs.MiscellaneousConfigs
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class MiscellaneousConfig(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, MiscellaneousConfig>(MiscellaneousConfigs)

	var enableBomDiaECia by MiscellaneousConfigs.enableBomDiaECia
	var enableQuirky by MiscellaneousConfigs.enableQuirky
}