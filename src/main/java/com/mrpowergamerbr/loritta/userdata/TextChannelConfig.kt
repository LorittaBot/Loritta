package com.mrpowergamerbr.loritta.userdata

class TextChannelConfig(val id: String) {
	constructor() : this("???")

	// Unused
	var isBlacklisted = false
	var automodConfig = AutomodConfig()
}