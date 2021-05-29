package net.perfectdreams.loritta.dao.servers.moduleconfigs

import net.perfectdreams.loritta.tables.servers.moduleconfigs.AutoroleConfigs
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class AutoroleConfig(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, AutoroleConfig>(AutoroleConfigs)

	var enabled by AutoroleConfigs.enabled
	var giveOnlyAfterMessageWasSent by AutoroleConfigs.giveOnlyAfterMessageWasSent
	var roles by AutoroleConfigs.roles
	var giveRolesAfter by AutoroleConfigs.giveRolesAfter
}