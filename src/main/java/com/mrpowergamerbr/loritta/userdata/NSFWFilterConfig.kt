package com.mrpowergamerbr.loritta.userdata

data class NSFWFilterConfig(
	var isEnabled: Boolean,
	var reportOnChannelId: String?,
	var reportMessage: String?,
	var removeMessage: Boolean,
	var warnMessage: String?,
	var ignoreChannels: MutableList<String>) {
	constructor() : this(false, null, null, true, null, mutableListOf<String>())
}