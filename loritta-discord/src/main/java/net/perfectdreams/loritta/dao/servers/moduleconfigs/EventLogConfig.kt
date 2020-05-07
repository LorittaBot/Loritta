package net.perfectdreams.loritta.dao.servers.moduleconfigs

import net.perfectdreams.loritta.tables.servers.moduleconfigs.EventLogConfigs
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class EventLogConfig(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, EventLogConfig>(EventLogConfigs)

	var enabled by EventLogConfigs.enabled
	var eventLogChannelId by EventLogConfigs.eventLogChannelId
	var memberBanned by EventLogConfigs.memberBanned
	var memberUnbanned by EventLogConfigs.memberUnbanned
	var messageEdited by EventLogConfigs.messageEdited
	var messageDeleted by EventLogConfigs.messageDeleted
	var nicknameChanges by EventLogConfigs.nicknameChanges
	var voiceChannelJoins by EventLogConfigs.voiceChannelJoins
	var voiceChannelLeaves by EventLogConfigs.voiceChannelLeaves
	var avatarChanges by EventLogConfigs.avatarChanges
}