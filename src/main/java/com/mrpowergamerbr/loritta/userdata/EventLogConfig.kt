package com.mrpowergamerbr.loritta.userdata

data class EventLogConfig(
	var isEnabled: Boolean, // Está ativado?
	var eventLogChannelId: String?, // ID do Canal
	var memberJoin: Boolean,
	var memberLeave: Boolean,
	var memberBanned: Boolean,
	var memberUnbanned: Boolean,
	var messageEdit: Boolean,
	var messageDeleted: Boolean,
	var bulkMessageDelete: Boolean,
	var channelCreated: Boolean,
	var roleCreated: Boolean,
	var roleDeleted: Boolean,
	var roleUpdated: Boolean,
	var roleGiven: Boolean,
	var roleRemoved: Boolean,
	var nicknameChanges: Boolean,
	var usernameChanges: Boolean,
	var avatarChanges: Boolean,
	var moderadorCommands: Boolean,
	var voiceChannelJoins: Boolean,
	var voiceChannelLeaves: Boolean,
	var voiceChannelMoves: Boolean,
	var inviteManager: Boolean
	) {
	constructor() : this(false, null, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false)
}