package com.mrpowergamerbr.loritta.userdata

class InviteBlockerConfig(var isEnabled: Boolean, var whitelistedIds: MutableList<String>, var whitelistedChannels: MutableList<String>, var whitelistServerInvites: Boolean, var deleteMessage: Boolean, var tellUser: Boolean, var warnMessage: String) {
	constructor() : this(false, mutableListOf<String>(), mutableListOf<String>(), true, true, true, "{@user} Você não pode enviar convites de outros servidores aqui!")
}