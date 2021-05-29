package net.perfectdreams.loritta.dao.servers.moduleconfigs

import net.perfectdreams.loritta.tables.servers.moduleconfigs.WarnActions
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class WarnAction(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, WarnAction>(WarnActions)

	var config by WarnActions.config
	var punishmentAction by WarnActions.punishmentAction
	var warnCount by WarnActions.warnCount
	var metadata by WarnActions.metadata
}