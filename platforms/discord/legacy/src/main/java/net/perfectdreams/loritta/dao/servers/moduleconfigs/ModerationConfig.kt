package net.perfectdreams.loritta.dao.servers.moduleconfigs

import net.perfectdreams.loritta.tables.servers.moduleconfigs.ModerationConfigs
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ModerationConfig(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, ModerationConfig>(ModerationConfigs)

	var sendPunishmentViaDm by ModerationConfigs.sendPunishmentViaDm
	var sendPunishmentToPunishLog by ModerationConfigs.sendPunishmentToPunishLog
	var punishLogChannelId by ModerationConfigs.punishLogChannelId
	var punishLogMessage by ModerationConfigs.punishLogMessage
}