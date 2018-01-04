package com.mrpowergamerbr.loritta.userdata

class JoinLeaveConfig {
	var isEnabled: Boolean = false
	var tellOnJoin: Boolean = true
	var tellOnLeave: Boolean = true
	var joinMessage: String = "\uD83D\uDC49 {@user} entrou no servidor!"
	var leaveMessage: String = "\uD83D\uDC48 {nickname} saiu do servidor!"
	var canalJoinId: String? = null
	var canalLeaveId: String? = null
	var tellOnPrivate: Boolean = false
	var joinPrivateMessage: String = "Obrigado por entrar na {guild} {@user}! Espero que você curta o nosso servidor!"
	var tellOnBan: Boolean = false
	var banMessage: String = ""
	var tellOnKick: Boolean = false
	var kickMessage: String = ""
}