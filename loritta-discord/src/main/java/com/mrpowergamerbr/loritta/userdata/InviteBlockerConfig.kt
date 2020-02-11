package com.mrpowergamerbr.loritta.userdata

class InviteBlockerConfig {
	var isEnabled: Boolean = false
	var whitelistedIds = mutableListOf<String>()
	var whitelistedChannels  = mutableListOf<String>()
	var whitelistServerInvites = true
	var deleteMessage = true
	var tellUser = true
	var warnMessage = "{@user} Você não pode enviar convites de outros servidores aqui!"
}