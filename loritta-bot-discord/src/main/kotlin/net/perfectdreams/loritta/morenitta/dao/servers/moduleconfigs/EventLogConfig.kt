package net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs

import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.EventLogConfigs
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class EventLogConfig(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, EventLogConfig>(EventLogConfigs)

	var enabled by EventLogConfigs.enabled
	var eventLogChannelId by EventLogConfigs.eventLogChannelId

	var memberBanned by EventLogConfigs.memberBanned
	var memberUnbanned by EventLogConfigs.memberUnbanned
	var memberJoin by EventLogConfigs.memberJoin
	var memberLeave by EventLogConfigs.memberLeave
	var memberRoleAdd by EventLogConfigs.memberRoleAdd
	var memberRoleRemove by EventLogConfigs.memberRoleRemove
	var memberTimeout by EventLogConfigs.memberTimeout

	var messageEdited by EventLogConfigs.messageEdited
	var messageDeleted by EventLogConfigs.messageDeleted
	var imageDeleted by EventLogConfigs.imageDeleted
	var bulkMessageDeleted by EventLogConfigs.bulkMessageDeleted

	var inviteCreated by EventLogConfigs.inviteCreated
	var moderatorCommands by EventLogConfigs.moderatorCommands

	var nicknameChanges by EventLogConfigs.nicknameChanges
	var avatarChanges by EventLogConfigs.avatarChanges

	var roleCreate by EventLogConfigs.roleCreate
	var roleDelete by EventLogConfigs.roleDelete
	var roleUpdate by EventLogConfigs.roleUpdate

	var channelCreate by EventLogConfigs.channelCreate
	var channelDelete by EventLogConfigs.channelDelete
	var channelUpdate by EventLogConfigs.channelUpdate

	var voiceChannelJoins by EventLogConfigs.voiceChannelJoins
	var voiceChannelLeaves by EventLogConfigs.voiceChannelLeaves
	var voiceChannelMove by EventLogConfigs.voiceChannelMove

	var memberBannedLogChannelId by EventLogConfigs.memberBannedLogChannelId
	var memberUnbannedLogChannelId by EventLogConfigs.memberUnbannedLogChannelId
	var memberJoinLogChannelId by EventLogConfigs.memberJoinLogChannelId
	var memberLeaveLogChannelId by EventLogConfigs.memberLeaveLogChannelId
	var memberRoleAddLogChannelId by EventLogConfigs.memberRoleAddLogChannelId
	var memberRoleRemoveLogChannelId by EventLogConfigs.memberRoleRemoveLogChannelId
	var memberTimeoutLogChannelId by EventLogConfigs.memberTimeoutLogChannelId

	var messageEditedLogChannelId by EventLogConfigs.messageEditedLogChannelId
	var messageDeletedLogChannelId by EventLogConfigs.messageDeletedLogChannelId
	var imageDeletedLogChannelId by EventLogConfigs.imageDeletedLogChannelId
	var bulkMessageDeletedLogChannelId by EventLogConfigs.bulkMessageDeletedLogChannelId

	var inviteCreatedLogChannelId by EventLogConfigs.inviteCreatedLogChannelId
	var moderatorCommandsLogChannelId by EventLogConfigs.moderatorCommandsLogChannelId

	var nicknameChangesLogChannelId by EventLogConfigs.nicknameChangesLogChannelId
	var avatarChangesLogChannelId by EventLogConfigs.avatarChangesLogChannelId

	var roleCreateLogChannelId by EventLogConfigs.roleCreateLogChannelId
	var roleDeleteLogChannelId by EventLogConfigs.roleDeleteLogChannelId
	var roleUpdateLogChannelId by EventLogConfigs.roleUpdateLogChannelId

	var channelCreateLogChannelId by EventLogConfigs.channelCreateLogChannelId
	var channelDeleteLogChannelId by EventLogConfigs.channelDeleteLogChannelId
	var channelUpdateLogChannelId by EventLogConfigs.channelUpdateLogChannelId

	var voiceChannelJoinsLogChannelId by EventLogConfigs.voiceChannelJoinsLogChannelId
	var voiceChannelLeavesLogChannelId by EventLogConfigs.voiceChannelLeavesLogChannelId
	var voiceChannelMoveLogChannelId by EventLogConfigs.voiceChannelMoveLogChannelId

	var updatedAt by EventLogConfigs.updatedAt
}
