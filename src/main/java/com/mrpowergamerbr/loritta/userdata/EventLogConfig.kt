package com.mrpowergamerbr.loritta.userdata

class EventLogConfig {
	var isEnabled: Boolean = false // Está ativado?
	var eventLogChannelId: String? = null // ID do Canal
	var memberJoin: Boolean = false
	var memberLeave: Boolean = false
	var memberBanned: Boolean = false
	var memberUnbanned: Boolean = false
	var messageEdit: Boolean = false
	var messageDeleted: Boolean = false
	var bulkMessageDelete: Boolean = false
	var channelCreated: Boolean = false
	var channelNameUpdated: Boolean = false
	var channelTopicUpdated: Boolean = false
	var channelPositionUpdated: Boolean = false
	var channelDeleted: Boolean = false
	var roleCreated: Boolean = false
	var roleDeleted: Boolean = false
	var roleUpdated: Boolean = false
	var roleGiven: Boolean = false
	var roleRemoved: Boolean = false
	var nicknameChanges: Boolean = false
	var usernameChanges: Boolean = false
	var avatarChanges: Boolean = false
	var moderatorCommands: Boolean = false
	var voiceChannelJoins: Boolean = false
	var voiceChannelLeaves: Boolean = false
	var voiceChannelMoves: Boolean = false
	var inviteManager: Boolean = false
}