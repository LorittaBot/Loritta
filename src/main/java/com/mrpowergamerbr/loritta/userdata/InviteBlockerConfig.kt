package com.mrpowergamerbr.loritta.userdata

class InviteBlockerConfig(var isEnabled: Boolean, var whitelistedIds: MutableList<String>, var whitelistedChannels: MutableList<String>, var whitelistServerInvites: Boolean) {
	constructor() : this(false, mutableListOf<String>(), mutableListOf<String>(), false)
}