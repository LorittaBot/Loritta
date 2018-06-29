package com.mrpowergamerbr.loritta.userdata

class JoinLeaveConfig {
	@AllowReflection
	var isEnabled: Boolean = false
	@AllowReflection
	var tellOnJoin: Boolean = true
	@AllowReflection
	var tellOnLeave: Boolean = true
	@AllowReflection
	var joinMessage: String = "\uD83D\uDC49 {@user} entrou no servidor!"
	@AllowReflection
	var leaveMessage: String = "\uD83D\uDC48 {nickname} saiu do servidor!"
	@AllowReflection
	var canalJoinId: String? = null
	@AllowReflection
	var canalLeaveId: String? = null
	@AllowReflection
	var tellOnPrivate: Boolean = false
	@AllowReflection
	var joinPrivateMessage: String = "Obrigado por entrar na {guild} {@user}! Espero que você curta o nosso servidor!"
	@AllowReflection
	var tellOnBan: Boolean = false
	@AllowReflection
	var banMessage: String = ""
	@AllowReflection
	var tellOnKick: Boolean = false
	@AllowReflection
	var kickMessage: String = ""
}