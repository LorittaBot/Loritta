package com.mrpowergamerbr.loritta.userdata

data class JoinLeaveConfig (
	var isEnabled: Boolean,
	var tellOnJoin: Boolean,
	var tellOnLeave: Boolean,
	var joinMessage: String,
	var leaveMessage: String,
	var canalJoinId: String?,
	var canalLeaveId: String?,
	var tellOnPrivate: Boolean,
	var joinPrivateMessage: String) {
	constructor() : this(false, true, true, "\uD83D\uDC49 {@user} entrou no servidor!", "\uD83D\uDC48 {nickname} saiu do servidor!", null, null, false, "Obrigado por entrar na {guild} {@user}! Espero que você curta o nosso servidor!")
}