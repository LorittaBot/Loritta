package com.mrpowergamerbr.loritta.userdata

class JoinLeaveConfig (
	var isEnabled: Boolean,
	var tellOnJoin: Boolean,
	var tellOnLeave: Boolean,
	var joinMessage: String,
	var leaveMessage: String,
	var canalJoinId: String?,
	var canalLeaveId: String?) {
	constructor() : this(false, true, true, "\uD83D\uDC49 {@user} entrou no servidor!", "\uD83D\uDC48 {nickname} saiu do servidor!", null, null)
}