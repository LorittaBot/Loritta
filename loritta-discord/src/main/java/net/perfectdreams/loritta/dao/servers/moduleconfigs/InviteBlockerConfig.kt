package net.perfectdreams.loritta.dao.servers.moduleconfigs

import net.perfectdreams.loritta.tables.servers.moduleconfigs.InviteBlockerConfigs
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class InviteBlockerConfig(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, InviteBlockerConfig>(InviteBlockerConfigs)

	var enabled by InviteBlockerConfigs.enabled
	var whitelistedChannels by InviteBlockerConfigs.whitelistedChannels
	var whitelistServerInvites by InviteBlockerConfigs.whitelistServerInvites
	var deleteMessage by InviteBlockerConfigs.deleteMessage
	var tellUser by InviteBlockerConfigs.tellUser
	var warnMessage by InviteBlockerConfigs.warnMessage
}